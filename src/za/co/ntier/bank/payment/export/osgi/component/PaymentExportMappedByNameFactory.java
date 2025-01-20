package za.co.ntier.bank.payment.export.osgi.component;

import org.adempiere.base.IPaymentExporterFactory;
import org.adempiere.base.MappedByNameFactory;
import org.compiere.util.PaymentExport;
import org.osgi.service.component.annotations.Component;

import za.co.ntier.bank.payment.export.eft.EftStandardBankDomesticExport;

@Component(
		 property= {"service.ranking:Integer=2"},
		 service = org.adempiere.base.IPaymentExporterFactory.class 
)
public class PaymentExportMappedByNameFactory extends MappedByNameFactory<PaymentExport> implements IPaymentExporterFactory{
	
	public PaymentExportMappedByNameFactory() {
		addMapping("EftStandardBankDomestic", () -> new EftStandardBankDomesticExport());
	}
	
	@Override
	public PaymentExport newPaymentExporterInstance(String className) {
		return newInstance(className);
	}

}
