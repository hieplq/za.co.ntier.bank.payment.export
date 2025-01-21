package za.co.ntier.bank.payment.export.eft;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import org.beanio.BeanIOConfigurationException;
import org.beanio.BeanWriter;
import org.beanio.StreamFactory;
import org.compiere.model.MPaySelectionCheck;
import org.compiere.util.PaymentExport;

public abstract class PaymentExportSupport implements PaymentExport{

	public abstract InputStream getEftMapping();
	
	public abstract String getStreamMappingName ();
	
	public abstract Iterator<Map.Entry<String, Map<String, Object>>> getLineIterator(MPaySelectionCheck[] checks, boolean depositBatch, String paymentRule, StringBuffer err);
	
	@Override
	public int exportToFile (MPaySelectionCheck[] checks, boolean depositBatch, String paymentRule, File file, StringBuffer err) {
		if (checks == null || checks.length == 0)
			return 0;
		
		int lineCount = 0;
		
		/* mapping file can be load from attachment */
		try(InputStream isEftMapping = getEftMapping();){
			
			StreamFactory eftSBDStreamFactory = StreamFactory.newInstance();
			eftSBDStreamFactory.load(isEftMapping);
			
			try (BeanWriter eftSBDBeanWriter = eftSBDStreamFactory.createWriter(getStreamMappingName(), file)){
				
				Iterator<Map.Entry<String, Map<String, Object>>> lineIterator = getLineIterator(checks, depositBatch, paymentRule, err);
				
				while (lineIterator.hasNext()) {
					Map.Entry<String, Map<String, Object>> line = lineIterator.next();
					eftSBDBeanWriter.write(line.getKey(), line.getValue());
					lineCount++;
				}

				eftSBDBeanWriter.flush();
			} catch (BeanIOConfigurationException e) {
				e.printStackTrace();
			}
		}catch (IOException eIO) {
			eIO.printStackTrace();
		}
	    
	    return lineCount;
	}
}
