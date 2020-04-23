package com.example.schooldata;

import com.alibaba.fastjson.JSONObject;
import com.example.schooldata.entity.Area;
import com.example.schooldata.entity.School;
import com.example.schooldata.entity.SchoolTypeEnum;
import com.example.schooldata.service.SchoolService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SchoolDataApplication.class)
public class SchoolDataApplicationTests {

    @Autowired
    private SchoolService schoolService;

    private final static String KEY = "your key";

    private final static String PARAMS_FORMAT = "city=%s&output=json&offset=25&page=%d&key=%s&extensions=all&citylimit=true&types=%s";

    private final static String URL = "https://restapi.amap.com/v3/place/text";

    private final static Integer PAGE_SIZE = 25;

    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    OkHttpClient client = new OkHttpClient();

    @Test
    public void testGetSchool() throws IOException {
        Map<String, SchoolTypeEnum> schoolMap = new HashMap<>();
        schoolMap.put("幼儿园", SchoolTypeEnum.NURSERY_SCHOOL);
        schoolMap.put("小学", SchoolTypeEnum.PRIMARY_SCHOOL);
        schoolMap.put("中学", SchoolTypeEnum.MIDDLE_SCHOOL);
        Resource resource = new ClassPathResource("areas.json");
        String inputJson = IOUtils.toString(resource.getInputStream(), Charset.defaultCharset());
        List<Area> areaList = JSONObject.parseArray(inputJson, Area.class);

        schoolMap.forEach((type, schoolType) ->
                areaList.forEach(area -> {
                    String areaCode = area.getCode();
                    // 获取数据
                    Result result = getData(1, areaCode, type);
                    if (Objects.nonNull(result) && !CollectionUtils.isEmpty(result.getPois())) {
                        // 保存数据
                        saveData(result.getPois(), schoolType);
                        int count = result.getCount();
                        int pageTotal = count % PAGE_SIZE == 0 ? count / PAGE_SIZE : count / PAGE_SIZE + 1;
                        // 多线程分页查询
                        for (int page = 2; page <= pageTotal; page++) {
                            executorService.execute(new GetDataThread(page, areaCode, type, schoolType));
                        }
                    } else {
                        log.error("{}, {}, {}", areaCode, type, schoolType);
                    }
                }));
        executorService.shutdown();
    }


    private Result getData(int page, String cityCode, String type) {
        String params = String.format(PARAMS_FORMAT, cityCode, page, KEY, type);
        String url = URL + "?" + params;
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (Objects.nonNull(response.body())) {
                String respStr = response.body().string();
                Result result = JSONObject.parseObject(respStr, Result.class);
                if (Objects.equals(result.getStatus(), 0)) {
                    log.error("{}, {}, {}, {} ", cityCode, type, page, result.getInfo());
                }
                return result;
            }
        } catch (IOException e) {
            log.error("{}, {}, {}, {}", cityCode, type, page, e.getMessage());
        }
        return null;
    }


    private void saveData(List<SchoolServer> schools, SchoolTypeEnum schoolType) {
        List<School> systemSchools = schools.stream().map(school -> {
            School systemSchool = new School();
            String areaCode = school.getAdcode();
            systemSchool.setName(school.getName());
            systemSchool.setSchoolType(schoolType);
            systemSchool.setAddress(school.getAddress());
            systemSchool.setProvince(school.getPname());
            systemSchool.setProvinceCode(areaCode.substring(0, 2));
            systemSchool.setCity(school.getCityname());
            systemSchool.setCityCode(areaCode.substring(0, 4));
            systemSchool.setArea(school.getAdname());
            systemSchool.setAreaCode(areaCode);
            systemSchool.setLocation(school.getLocation());
            return systemSchool;
        }).collect(Collectors.toList());
        schoolService.saveBatch(systemSchools);
    }

    private class GetDataThread implements Runnable {

        int page;
        String areaCode;
        String type;
        SchoolTypeEnum schoolType;

        GetDataThread(int page, String areaCode, String type, SchoolTypeEnum schoolType) {
            this.page = page;
            this.areaCode = areaCode;
            this.type = type;
            this.schoolType = schoolType;
        }

        @Override
        public void run() {
            Result result = getData(page, areaCode, type);
            if (Objects.nonNull(result) && !CollectionUtils.isEmpty(result.getPois())) {
                saveData(result.getPois(), schoolType);
            } else {
                log.error("{}, {}, {}, {}", areaCode, type, schoolType, page);
            }
        }
    }


    @Data
    private static class Result implements Serializable {
        private static final long serialVersionUID = -8998271790023903532L;
        /**
         * 0：请求失败；1：请求成功
         */
        private Integer status;
        /**
         * status为0时，info返回错误原因，否则返回“OK”
         */
        private String info;
        /**
         * 总数
         */
        private Integer count;

        private List<SchoolServer> pois;
    }

    @Data
    private static class SchoolServer implements Serializable {
        private static final long serialVersionUID = -4125461625735295834L;
        /**
         * 学校名称
         */
        private String name;
        /**
         * 别名
         */
        private String alias;
        /**
         * 经纬度
         */
        private String location;
        /**
         * 县级编码
         */
        private String adcode;
        /**
         * 省
         */
        private String pname;
        /**
         * 市
         */
        private String cityname;

        /**
         * 县
         */
        private String adname;
        /**
         * 地址
         */
        private String address;

    }

}
