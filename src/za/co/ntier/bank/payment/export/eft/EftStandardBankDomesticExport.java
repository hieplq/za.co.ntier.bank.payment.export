package za.co.ntier.bank.payment.export.eft;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.compiere.model.MBPBankAccount;
import org.compiere.model.MPaySelectionCheck;
import org.compiere.util.Env;
import org.compiere.util.Msg;

public class EftStandardBankDomesticExport extends PaymentExportSupport {
	public static enum RecordType {
		HEADER("header"), DETAIL("detail"), TRAILER("trailer");
		
		private final String recordType; 

		RecordType(String recordType) {
	        this.recordType = recordType;
	    }
		
		@Override
	    public String toString() {
	        return this.recordType; 
	    }
	}
	
	@Override
	public InputStream getEftMapping() {
		// can improve to get mapping from attachment
        return EftStandardBankDomesticExport.class.getClassLoader().getResourceAsStream("mapping/eftStandardBankDomestic.beanio.xml");
        
	}
	
	@Override
	public String getStreamMappingName() {
		return "eftStandardBankDomestic";
	}
	
	public Map<String, Object> buildEftSbdHeader(MPaySelectionCheck[] checks, boolean depositBatch, String paymentRule, StringBuffer err) {
		Map<String, Object> eftSBDHeader = new HashMap<>();
		eftSBDHeader.put("compCode", "592C"); //TODO: We will put this eventually under the Bank window as a company code field. but not yet defined on 2pac
		eftSBDHeader.put("compName", "MQA Operations"); //TODO: same as above
	    
		Calendar currentDate = Calendar.getInstance();
		eftSBDHeader.put("actDate", currentDate);
		
		eftSBDHeader.put("stmRef", "MQA");//TODO: This will eventually be a field on payment selection (manual)
		
		return eftSBDHeader;
	}

	public List<Map<String, Object>> buildEftSbdDetail(MPaySelectionCheck[] checks, boolean depositBatch, String paymentRule, StringBuffer err) {
		List<Map<String, Object>> eftSbdDetailData = new ArrayList<>();
		
		int empNum = 0;
		boolean isFoundBankAcc = false;
		
		for (MPaySelectionCheck check : checks) {
			MBPBankAccount[] bpBankAcc = MBPBankAccount.getOfBPartner(Env.getCtx(), check.getC_BPartner_ID());
			
			for (MBPBankAccount bpSbdBankAcc : bpBankAcc) {
				// get first bank account has value for branch number
				String branchNum = bpSbdBankAcc.get_ValueAsString("ZZ_Branch_Number").trim();
				if (StringUtils.isNotEmpty(branchNum) && bpSbdBankAcc.get_ValueAsBoolean("ZZ_Approve")) {
					Map<String, Object> eftSbdDetailLine = new HashMap<>();
					BigDecimal atm = check.getPayAmt().multiply(new BigDecimal(100));
					eftSbdDetailLine.put("amt", atm);//always rand?
					eftSbdDetailLine.put("compCode", "592C"); //TODO: We will put this eventually under the Bank window as a company code field. but not yet defined on 2pac
					
					eftSbdDetailLine.put("branchNum", Integer.valueOf(branchNum));
					eftSbdDetailLine.put("accName", bpSbdBankAcc.getA_Name());
					eftSbdDetailLine.put("accNum", Long.valueOf(bpSbdBankAcc.getAccountNo()));
					empNum++;
					eftSbdDetailLine.put("empNum", empNum);
					eftSbdDetailData.add(eftSbdDetailLine);
					isFoundBankAcc = true;
					break;
				}
			}
		}
		
		if (!isFoundBankAcc) {
			err.append(Msg.getMsg(Env.getCtx(), "ZZ_BpartnerNonApprovedBankAccount"));
		}
		return eftSbdDetailData;
	}
	
	public Map<String, Object> buildEftSbdTrailer(Map<String, Object> eftSBDHeader, List<Map<String, Object>> eftSbdDetailData) {
		Map<String, Object> eftSBDTrailer = new HashMap<>();
		
		eftSBDTrailer.put("compCode", eftSBDHeader.get("compCode"));
		eftSBDTrailer.put("numTrans", eftSbdDetailData.size());
		
		BigDecimal amount = new BigDecimal(0);
		
		for (Map<String, Object> eftSBDDetailLine : eftSbdDetailData) {
			amount = amount.add((BigDecimal) eftSBDDetailLine.get("amt"));
		}
		
		eftSBDTrailer.put("amt", amount);
		
		return eftSBDTrailer;
	}

	@Override
	public Iterator<Entry<String, Map<String, Object>>> getLineIterator(MPaySelectionCheck[] checks, boolean depositBatch, String paymentRule, StringBuffer err) {
		List<Map<String, Object>> eftSbdDetailData = buildEftSbdDetail(checks, depositBatch, paymentRule, err);
		if (err.length() > 0) {
			return null;
		}
		Map<String, Object>  eftSBDHeader = buildEftSbdHeader(checks, depositBatch, paymentRule, err);
		Map<String, Object> eftSBDTrailer = buildEftSbdTrailer(eftSBDHeader, eftSbdDetailData);
		
		List<Entry<String, Map<String, Object>>> lines = new ArrayList<>();
		lines.add(new AbstractMap.SimpleImmutableEntry<>(RecordType.HEADER.toString(), eftSBDHeader));
		
		eftSbdDetailData.stream().forEachOrdered(eftSbdDetailLine -> {
			lines.add(new AbstractMap.SimpleImmutableEntry<>(RecordType.DETAIL.toString(), eftSbdDetailLine));
		});
		
		lines.add(new AbstractMap.SimpleImmutableEntry<>(RecordType.TRAILER.toString(), eftSBDTrailer));

		return lines.iterator();
	}
}
