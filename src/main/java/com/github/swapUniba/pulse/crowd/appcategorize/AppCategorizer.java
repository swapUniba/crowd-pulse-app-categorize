package com.github.swapUniba.pulse.crowd.appcategorize;

import com.github.frapontillo.pulse.crowd.data.entity.PersonalData;
import com.github.frapontillo.pulse.spi.IPlugin;
import com.github.frapontillo.pulse.spi.VoidConfig;
import com.github.frapontillo.pulse.util.PulseLogger;
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
public class AppCategorizer extends IPlugin<PersonalData, PersonalData, VoidConfig> {

    private static final String PLUGIN_NAME = "app-categorize";
    private static final String APP_INFO = "appinfo";
    private Logger logger = PulseLogger.getLogger(AppCategorizer.class);

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public VoidConfig getNewParameter() {
        return new VoidConfig();
    }

    @Override
    protected Operator<PersonalData, PersonalData> getOperator(VoidConfig params) {
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

                    GooglePlayCategorizer categorizer = new GooglePlayCategorizer(appInfo.getPackageName());
                    appInfo.setCategory(categorizer.getCategory());

                    logger.info("App package name: " + appInfo.getPackageName() + " -- Category found: " + appInfo.getCategory());

                    reportElementAsEnded(appInfo.getPackageName());
                }
                subscriber.onNext(appInfo);
            }
        });
    }

}