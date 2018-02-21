package com.github.swapUniba.pulse.crowd.appcategorize;

import com.github.frapontillo.pulse.crowd.data.entity.PersonalData;
import com.github.frapontillo.pulse.spi.IPlugin;
import com.github.frapontillo.pulse.spi.IPluginConfig;
import com.github.frapontillo.pulse.spi.PluginConfigHelper;
import com.github.frapontillo.pulse.util.PulseLogger;
import com.google.gson.JsonElement;
import org.apache.logging.log4j.Logger;
import rx.Observable.Operator;
import rx.Subscriber;
import rx.observers.SafeSubscriber;

/**
 * CrowdPulse's App categorize plugin.
 *
 * @author Cosimo Lovascio
 * @author Fabio De Pasquale
 *
 */
public class AppCategorizer extends IPlugin<PersonalData, PersonalData, AppCategorizer.AppCategorizerConfig> {

    private static final String PLUGIN_NAME = "app-categorize";
    private static final String APP_INFO = "appinfo";
    private Logger logger = PulseLogger.getLogger(AppCategorizer.class);

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public AppCategorizerConfig getNewParameter() {
        return new AppCategorizerConfig();
    }

    @Override
    protected Operator<PersonalData, PersonalData> getOperator(AppCategorizerConfig params) {
        return subscriber -> new SafeSubscriber<>(new Subscriber<PersonalData>() {

            @Override
            public void onCompleted() {
                reportPluginAsCompleted();
                subscriber.onCompleted();
            }

            @Override
            public void onError(Throwable e) {
                reportPluginAsErrored();
                subscriber.onError(e);
            }

            @Override
            public void onNext(PersonalData appInfo) {
                if (appInfo.getSource().equals(APP_INFO)) {
                    reportElementAsStarted(appInfo.getPackageName());
                    GooglePlayCategorizer categorizer;

                    if (params != null) {
                        switch (params.getCalculate()) {
                            case AppCategorizerConfig.ALL:
                                categorizer = new GooglePlayCategorizer(appInfo.getPackageName());
                                appInfo.setCategory(categorizer.getCategory());
                                logger.info("App package name: " + appInfo.getPackageName() + " -- Category found: " + appInfo.getCategory());
                                break;

                            case AppCategorizerConfig.NEW:
                                if (appInfo.getCategory() == null) {
                                    categorizer = new GooglePlayCategorizer(appInfo.getPackageName());
                                    appInfo.setCategory(categorizer.getCategory());
                                    logger.info("App package name: " + appInfo.getPackageName() + " -- Category found: " + appInfo.getCategory());
                                } else {
                                    logger.info("App skipped (category already exists)");
                                }
                                break;

                            default:
                                categorizer = new GooglePlayCategorizer(appInfo.getPackageName());
                                appInfo.setCategory(categorizer.getCategory());
                                logger.info("App package name: " + appInfo.getPackageName() + " -- Category found: " + appInfo.getCategory());
                                break;
                        }
                    } else {

                        // as default case
                        categorizer = new GooglePlayCategorizer(appInfo.getPackageName());
                        appInfo.setCategory(categorizer.getCategory());
                        logger.info("App package name: " + appInfo.getPackageName() + " -- Category found: " + appInfo.getCategory());
                    }

                    reportElementAsEnded(appInfo.getPackageName());
                }
                subscriber.onNext(appInfo);
            }
        });
    }


    class AppCategorizerConfig implements IPluginConfig<AppCategorizerConfig> {

        /**
         * Get the category of all applications coming from the stream.
         */
        public static final String ALL = "all";

        /**
         * Get the category of the applications with no category (property is null).
         */
        public static final String NEW = "new";


        /**
         * Accepted values: NEW, ALL.
         * Default: ALL.
         */
        private String calculate;

        @Override
        public AppCategorizerConfig buildFromJsonElement(JsonElement jsonElement) {
            return PluginConfigHelper.buildFromJson(jsonElement, AppCategorizerConfig.class);
        }

        public String getCalculate() {
            return calculate;
        }

        public void setCalculate(String calculate) {
            this.calculate = calculate;
        }
    }

}