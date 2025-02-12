package za.co.ntier.bank.payment.export.osgi.component;

import org.adempiere.base.AnnotationBasedModelFactory;
import org.adempiere.base.IModelFactory;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true, service = IModelFactory.class, property = "service.ranking:Integer=1")
public class AnnotationModelFactory extends AnnotationBasedModelFactory {

}
