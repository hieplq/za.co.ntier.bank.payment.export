package za.co.ntier.bank.payment.export.eft;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import org.adempiere.exceptions.AdempiereException;
import org.beanio.BeanIOConfigurationException;
import org.beanio.BeanWriter;
import org.beanio.StreamFactory;
import org.compiere.model.MPaySelectionCheck;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.PaymentExport;

public abstract class PaymentExportSupport implements PaymentExport{
	static protected CLogger s_log = CLogger.getCLogger (PaymentExportSupport.class);
	
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
				
				if (err.length() > 0)
					return -1;
				
				while (lineIterator.hasNext()) {
					Map.Entry<String, Map<String, Object>> line = lineIterator.next();
					eftSBDBeanWriter.write(line.getKey(), line.getValue());
					lineCount++;
				}

				eftSBDBeanWriter.flush();
				complete();
			} catch (BeanIOConfigurationException e) {
				s_log.log(Level.SEVERE, e.getMessage());
				err.append(Msg.getMsg(Env.getCtx(), "ZZ_BeanIOConfigurationError"));
				return -1;
			}
		}catch (IOException eIO) {
			s_log.log(Level.SEVERE, eIO.getMessage());
			err.append(Msg.getMsg(Env.getCtx(), "ZZ_PaymentExportIOError"));
			return -1;
		}catch (AdempiereException aEx) {
			s_log.log(Level.SEVERE, aEx.getMessage());
			err.append(aEx.getMessage());
			return -1;
		}
	    
	    return lineCount;
	}
	
	public void complete () {
		
	}
}
