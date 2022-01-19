package com.ptfmobile.vn.authservice.util;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.TextSearchOptions;
import com.ptfmobile.vn.common.AppUtils;
import com.ptfmobile.vn.common.BaseAdminGetListRequest;
import org.bson.conversions.Bson;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author MinhDV
 */
public class QueryUtils {


    public static List<Bson> buildQueryListAdmin(BaseAdminGetListRequest request) throws ParseException {
        List<Bson> params = new ArrayList<>();

        if (!AppUtils.isNullOrEmpty(request.getKeyword())) {
            params.add(Filters.text(request.getKeyword(), new TextSearchOptions().caseSensitive(true)));
        }
        //field condition
        if (!CollectionUtils.isEmpty(request.getConditions())) {
            //gia tri ngan cach nhau boi dau ;
            List<String> dateFields = Arrays.asList("createTime", "createTime", "closedDate");//closed_date

            for (BaseAdminGetListRequest.BaseInfo baseInfo : request.getConditions()) {
                List<Object> valueList = baseInfo.getValue();
                String key = baseInfo.getKey();
                if (CollectionUtils.isEmpty(valueList) || AppUtils.isNullOrEmpty(key))
                    continue;
                if (key.equalsIgnoreCase("_id")) {

                    if (!valueList.isEmpty()) {
                        params.add(Filters.in("_id", valueList));
                    }
                } else if (dateFields.contains(key) && valueList.size() > 1) {
                    params.add(
                            Filters.and(
                                    Filters.gte(key, java.text.DateFormat.getDateInstance().parse(valueList.get(0).toString())),
                                    Filters.lte(key, java.text.DateFormat.getDateInstance().parse(valueList.get(1).toString()))
                            )
                    );
                } else {
                    params.add(Filters.in(
                            key, valueList
                    ));
                }
            }
        }

        if (params.isEmpty()) {
            params.add(Filters.exists("_id", true));
        }
        return params;
    }
}
