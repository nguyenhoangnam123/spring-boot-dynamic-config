package com.ptfmobile.vn.authservice.util;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import com.ptfmobile.vn.authservice.domain.BaseClazz;
import com.ptfmobile.vn.common.annotation.ModuleDescriptionAPI;
import com.ptfmobile.vn.common.AppUtils;
import com.ptfmobile.vn.common.DBConstant;
import com.ptfmobile.vn.db.dto.ModuleAPI;
import com.ptfmobile.vn.mongodb.BaseMongoDao;
import org.bouncycastle.math.raw.Mod;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author MinhDV
 */

@Component
public class GenerateModulePath extends BaseClazz implements CommandLineRunner {

    // class de tu dong lay tat ca cac path module va add vao DB,
    // phuc vu cho viec phan quyen theo path api

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final BaseMongoDao baseMongoDao;

    @Autowired
    public GenerateModulePath(MongoDatabase mongoDatabase,
                              RequestMappingHandlerMapping requestMappingHandlerMapping
    ) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.baseMongoDao = new BaseMongoDao(mongoDatabase);

    }

    @Override
    public void run(String... args) {
        String key = "GenerateModulePath";
        String serviceName = "auth";
        baseLog(key, "Start genListModule");
        List<ModuleAPI> moduleAPIS = genListModule(requestMappingHandlerMapping, serviceName);
        baseLog(key, "End genListModule size = ", moduleAPIS.size());
        //delete old module path

        if (!AppUtils.isListNullOrEmpty(moduleAPIS)) {
            List<String> listKeyHash = new ArrayList<>();
            List<WriteModel<Document>> updateList = new ArrayList<>();
            for (ModuleAPI moduleAPI : moduleAPIS) {
                updateList.add(buildReplaceModel(moduleAPI));

                listKeyHash.add(moduleAPI.getHashKey());
            }


            baseLog(key, "Start insert list module");
            long res = baseMongoDao.bulkUpdate(DBConstant.TableName.MODULE_API, updateList);
            baseLog(key, "End insert list module res = " + res);

            //remove unused api
            Bson conditionRemove = Filters.and(
                    Filters.eq("serviceName", serviceName),
                    Filters.nin("moduleId", listKeyHash)
            );

            baseLog(key, "Start remove list module");
            res = baseMongoDao.deleteMany(DBConstant.TableName.MODULE_API, conditionRemove);
            baseLog(key, "End remove list module res = " + res);
        }
    }

    private WriteModel<Document> buildReplaceModel(ModuleAPI moduleAPI) {
        Bson condition = Filters.and(Filters.eq("moduleId", moduleAPI.getModuleId()),
                Filters.eq("module", moduleAPI.getModule()));
        return new ReplaceOneModel<>(condition, moduleAPI.toDoc(), new UpdateOptions().upsert(true));
    }

    private List<ModuleAPI> genListModule(RequestMappingHandlerMapping requestMappingHandlerMapping,
                                          String serviceName) {

        List<ModuleAPI> moduleAPIS = new ArrayList<>();
        requestMappingHandlerMapping.getHandlerMethods().values().forEach(handlerMethod -> {
            if (handlerMethod != null) {
                Annotation[] declaredAnnotations = handlerMethod.getMethod().getDeclaredAnnotations();
                if (declaredAnnotations != null && declaredAnnotations.length > 0) {
                    if (handlerMethod.getMethod().isAnnotationPresent(ModuleDescriptionAPI.class)) {
                        String name = handlerMethod.getMethod().getAnnotation(ModuleDescriptionAPI.class).name();
                        String description = handlerMethod.getMethod().getAnnotation(ModuleDescriptionAPI.class).description();
                        int type = handlerMethod.getMethod().getAnnotation(ModuleDescriptionAPI.class).type();
                        String method = handlerMethod.getMethod().getAnnotation(ModuleDescriptionAPI.class).method();
                        String path = handlerMethod.getMethod().getAnnotation(ModuleDescriptionAPI.class).path();
                        String module = handlerMethod.getMethod().getAnnotation(ModuleDescriptionAPI.class).module();
                        boolean adminMetaData = handlerMethod.getMethod().getAnnotation(ModuleDescriptionAPI.class).adminMetadata();

                        ModuleAPI moduleAPI = new ModuleAPI();
                        moduleAPI.setModule(module);
                        moduleAPI.setName(name);
                        moduleAPI.setDescription(description);
                        moduleAPI.setMethod(method);
                        moduleAPI.setType(type);
                        if (adminMetaData)
                            moduleAPI.setMetaData(ModuleAPI.defaultMetaData);
                        else
                            moduleAPI.setMetaData(new HashMap<>());
                        moduleAPI.setCreateBy("SYSTEM");
                        moduleAPI.setApiPath(path);
                        moduleAPI.setServiceName(serviceName);

                        String hash = moduleAPI.getHashKey();
                        moduleAPI.setModuleId(hash);

                        moduleAPIS.add(moduleAPI);
                    }


                }
            }
        });
        return moduleAPIS;
    }
}
