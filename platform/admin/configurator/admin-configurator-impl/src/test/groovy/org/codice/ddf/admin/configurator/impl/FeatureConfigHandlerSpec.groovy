package org.codice.ddf.admin.configurator.impl

import ddf.security.permission.Permissions
import ddf.security.permission.impl.PermissionsImpl
import org.apache.karaf.features.Feature
import org.apache.karaf.features.FeatureState
import org.apache.karaf.features.FeaturesService
import org.apache.shiro.authz.Permission
import org.apache.shiro.subject.Subject
import org.codice.ddf.admin.configurator.ConfiguratorException
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceReference
import spock.lang.Specification

import static org.apache.karaf.features.FeaturesService.Option.NoAutoRefreshBundles

class FeatureConfigHandlerSpec extends Specification {
    public static final String FEATURE_NAME_AND_VERSION = 'xxx/0.1.0'
    private ServiceReference serviceReference
    private FeaturesService featuresService
    private BundleContext bundleContext
    private Subject subject

    private ServiceReference permissionsServiceRef
    private Permissions permissions = new PermissionsImpl()

    def setup() {
        featuresService = Mock(FeaturesService)
        def feature = Mock(Feature)
        feature.getName() >> 'xxx'
        feature.getVersion() >> '0.1.0'
        featuresService.getFeature('xxx') >> feature

        serviceReference = Mock(ServiceReference)

        bundleContext = Mock(BundleContext)
        bundleContext.getServiceReference(FeaturesService) >> serviceReference
        bundleContext.getService(serviceReference) >> featuresService

        bundleContext.getServiceReference(Permissions) >> permissionsServiceRef
        bundleContext.getService(permissionsServiceRef) >> permissions

        subject = Mock(Subject)
        subject.isPermitted(_ as Permission) >> true
    }

    def 'test start feature that was stopped and rollback'() {
        setup:
        featuresService.getState(FEATURE_NAME_AND_VERSION) >>> [FeatureState.Installed, FeatureState.Started]
        def handler = new FeatureOperation('xxx', true, bundleContext, subject, permissions)

        when:
        handler.commit()

        then:
        1 * featuresService.installFeature('xxx', EnumSet.of(NoAutoRefreshBundles))

        when:
        handler.rollback()

        then:
        1 * featuresService.uninstallFeature('xxx')
    }

    def 'test stop feature that was started and rollback'() {
        setup:
        featuresService.getState(FEATURE_NAME_AND_VERSION) >>> [FeatureState.Started, FeatureState.Installed]
        def handler = new FeatureOperation('xxx', false, bundleContext, subject, permissions)

        when:
        handler.commit()

        then:
        1 * featuresService.uninstallFeature('xxx')

        when:
        handler.rollback()

        then:
        1 * featuresService.installFeature('xxx', EnumSet.of(NoAutoRefreshBundles))
    }

    def 'test start feature that was already started and rollback'() {
        setup:
        featuresService.getState(FEATURE_NAME_AND_VERSION) >> FeatureState.Started
        def handler = new FeatureOperation('xxx', true, bundleContext, subject, permissions)

        when:
        handler.commit()

        then:
        0 * featuresService.installFeature('xxx', EnumSet.of(NoAutoRefreshBundles))
        0 * featuresService.uninstallFeature('xxx')

        when:
        handler.rollback()

        then:
        0 * featuresService.installFeature('xxx', EnumSet.of(NoAutoRefreshBundles))
        0 * featuresService.uninstallFeature('xxx')
    }

    def 'test stop feature that was already stopped and rollback'() {
        setup:
        featuresService.getState(FEATURE_NAME_AND_VERSION) >> FeatureState.Installed
        def handler = new FeatureOperation('xxx', false, bundleContext, subject, permissions)

        when:
        handler.commit()

        then:
        0 * featuresService.installFeature('xxx', EnumSet.of(NoAutoRefreshBundles))
        0 * featuresService.uninstallFeature('xxx')

        when:
        handler.rollback()

        then:
        0 * featuresService.installFeature('xxx', EnumSet.of(NoAutoRefreshBundles))
        0 * featuresService.uninstallFeature('xxx')
    }

    def 'test start feature that was stopped and rollback with no permissions'() {
        setup:
        featuresService.getState(FEATURE_NAME_AND_VERSION) >>> [FeatureState.Installed, FeatureState.Started]
        subject = Mock(Subject)
        subject.isPermitted(_ as Permission) >> false
        def handler = new FeatureOperation('xxx', true, bundleContext, subject, permissions)

        when:
        handler.commit()

        then:
        thrown(ConfiguratorException)
    }
}
