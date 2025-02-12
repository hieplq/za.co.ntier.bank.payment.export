package za.co.ntier.bank.payment.export.eft;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.adempiere.exceptions.AdempiereException;
import org.apache.commons.lang3.StringUtils;
import org.compiere.model.I_C_BankAccount;
import org.compiere.model.MBPBankAccount;
import org.compiere.model.MBPartner;
import org.compiere.model.MPaySelectionCheck;
import org.compiere.model.Query;
import org.compiere.util.Env;
import org.compiere.util.Msg;

import za.co.ntier.bank.payment.export.model.I_ZZ_BankCode;
import za.co.ntier.bank.payment.export.model.X_ZZ_BankCode;

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
	
	public Map<String, Object> buildEftSbdHeader(MPaySelectionCheck[] checks, I_ZZ_BankCode bankCode, boolean depositBatch, String paymentRule, StringBuffer err) {
		Map<String, Object> eftSBDHeader = new HashMap<>();
		
		eftSBDHeader.put("compCode", bankCode.getValue());
		eftSBDHeader.put("compName", bankCode.getName());
	    
		Calendar currentDate = Calendar.getInstance();
		eftSBDHeader.put("actDate", currentDate);
		
		eftSBDHeader.put("stmRef", "MQA");//TODO: This will eventually be a field on payment selection (manual)
		
		return eftSBDHeader;
	}

	public static X_ZZ_BankCode getBankCode(int bankAccountID) {
		return new Query(Env.getCtx(), I_ZZ_BankCode.Table_Name, 
					String.format("%s = ?", I_ZZ_BankCode.COLUMNNAME_C_BankAccount_ID), null)
						.setParameters(bankAccountID)
						.setOrderBy(String.format("%s ASC NULLS FIRST", I_ZZ_BankCode.COLUMNNAME_DateLastAction))
						.first();
	}
	
	public List<Map<String, Object>> buildEftSbdDetail(MPaySelectionCheck[] checks, I_ZZ_BankCode bankCode, boolean depositBatch, String paymentRule, StringBuffer err) {
		List<Map<String, Object>> eftSbdDetailData = new ArrayList<>();
		
		int empNum = 0;
		boolean isFoundBankAcc = false;
		
		List<MPaySelectionCheck> lsCheck = Arrays.asList(checks);
		Collections.sort(lsCheck, new Comparator<MPaySelectionCheck>() {
			@Override
			public int compare(MPaySelectionCheck o1, MPaySelectionCheck o2) {
				return o1.getC_BPartner_ID() - o2.getC_BPartner_ID();
			}
		});
		
		List<Integer> bPartnerNotFoundApprovalAccts = new ArrayList<>();
		
		for (MPaySelectionCheck check : checks) {
			MBPBankAccount[] bpBankAcc = MBPBankAccount.getOfBPartner(Env.getCtx(), check.getC_BPartner_ID());
			
			for (MBPBankAccount bpSbdBankAcc : bpBankAcc) {
				// get first bank account has value for branch number
				String branchNum = bpSbdBankAcc.get_ValueAsString("ZZ_Branch_Number").trim();
				if (StringUtils.isNotEmpty(branchNum) && bpSbdBankAcc.get_ValueAsBoolean("ZZ_Approve")) {
					Map<String, Object> eftSbdDetailLine = new HashMap<>();
					BigDecimal atm = check.getPayAmt().multiply(new BigDecimal(100));
					eftSbdDetailLine.put("amt", atm);//always rand?
					eftSbdDetailLine.put("compCode", bankCode.getValue());
					
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
			
			if (!isFoundBankAcc && !bPartnerNotFoundApprovalAccts.contains(check.getC_BPartner_ID())) {
				bPartnerNotFoundApprovalAccts.add(check.getC_BPartner_ID());
			}
			isFoundBankAcc = false;
		}
		
		if(!bPartnerNotFoundApprovalAccts.isEmpty()) {
			StringBuilder bPartnerNotFoundApprovalAcctNames = new StringBuilder();
			for(Integer bPartnerNotFoundApprovalAcct : bPartnerNotFoundApprovalAccts) {
				MBPartner partner = MBPartner.get(Env.getCtx(), bPartnerNotFoundApprovalAcct);
				if(!bPartnerNotFoundApprovalAcctNames.isEmpty()) {
                    bPartnerNotFoundApprovalAcctNames.append(", ");
                }
				bPartnerNotFoundApprovalAcctNames.append(partner.getName());
			}
			
			err.append(Msg.getMsg(Env.getCtx(), "ZZ_BpartnerNonApprovedBankAccount", new Object [] {bPartnerNotFoundApprovalAcctNames.toString()}));
		}
		
		return eftSbdDetailData;
	}
	
	public Map<String, Object> buildEftSbdTrailer(I_ZZ_BankCode bankCode, List<Map<String, Object>> eftSbdDetailData) {
		Map<String, Object> eftSBDTrailer = new HashMap<>();
		
		eftSBDTrailer.put("compCode", bankCode.getValue());
		eftSBDTrailer.put("numTrans", eftSbdDetailData.size());
		
		BigDecimal amount = new BigDecimal(0);
		
		for (Map<String, Object> eftSBDDetailLine : eftSbdDetailData) {
			amount = amount.add((BigDecimal) eftSBDDetailLine.get("amt"));
		}
		
		eftSBDTrailer.put("amt", amount);
		
		return eftSBDTrailer;
	}

	X_ZZ_BankCode bankCode = null;
	
	@Override
	public Iterator<Entry<String, Map<String, Object>>> getLineIterator(MPaySelectionCheck[] checks, boolean depositBatch, String paymentRule, StringBuffer err) {
		bankCode = EftStandardBankDomesticExport.getBankCode(checks[0].getC_PaySelection().getC_BankAccount_ID());
		if (bankCode == null) {
			I_C_BankAccount bankAcc = checks[0].getC_PaySelection().getC_BankAccount();
			throw new AdempiereException(Msg.getMsg(Env.getCtx(), "ZZ_BankCodeMissing", new Object [] {bankAcc.getName()}));
		}
		
		bankCode.set_UseOptimisticLocking(true);
		
		List<Map<String, Object>> eftSbdDetailData = buildEftSbdDetail(checks, bankCode, depositBatch, paymentRule, err);
		if (err.length() > 0) {
			return null;
		}
		Map<String, Object>  eftSBDHeader = buildEftSbdHeader(checks, bankCode, depositBatch, paymentRule, err);
		Map<String, Object> eftSBDTrailer = buildEftSbdTrailer(bankCode, eftSbdDetailData);
		
		List<Entry<String, Map<String, Object>>> lines = new ArrayList<>();
		lines.add(new AbstractMap.SimpleImmutableEntry<>(RecordType.HEADER.toString(), eftSBDHeader));
		
		eftSbdDetailData.stream().forEachOrdered(eftSbdDetailLine -> {
			lines.add(new AbstractMap.SimpleImmutableEntry<>(RecordType.DETAIL.toString(), eftSbdDetailLine));
		});
		
		lines.add(new AbstractMap.SimpleImmutableEntry<>(RecordType.TRAILER.toString(), eftSBDTrailer));

		return lines.iterator();
	}
	
	@Override
	public void complete() {
		super.complete();
		if (bankCode != null) {
			bankCode.setDateLastAction(Timestamp.valueOf(LocalDateTime.now()));
			try {
				bankCode.saveEx(null);
			}catch (AdempiereException e) {
				if (e.getMessage().contains("Could not save changes: Update return 0 instead of 1"))
					throw new AdempiereException(Msg.getMsg(Env.getCtx(), "ZZ_BankAccountOptimisticLocking"));
			}
			
		}
	}
}
