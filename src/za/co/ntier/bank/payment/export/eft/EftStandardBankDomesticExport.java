package za.co.ntier.bank.payment.export.eft;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.compiere.model.MPaySelectionCheck;

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
	
	public final String eftSBDMappingContent = """
			<beanio xmlns="http://www.beanio.org/2012/03">
				<!-- 'strict' enforces record order and record sizes -->
				<stream name="eftStandardBankDomestic" format="fixedlength" strict="true">
					<!-- 'occurs' enforces minimum and maximum record occurrences -->
			 		<record name="header" class="map" occurs="1" order="1" >
			   			<field name="errFlag" literal="*" length="1" />
			   			<field name="compCode" length="4" />
			   			<field name="compName" length="40" />
			   			<field name="actDate" length="8" padding="0" justify="right" />
			   			<field name="lang" literal="Y" length="1" />
			   			<field name="separate1"  length="8" padding=" " />
			   			<field name="stmRef" length="15" />
			   			<field name="sign" literal="+" length="1" />
			   			<field name="sec" literal="81" length="2" padding="0" justify="right" />
			   			<field name="taxCode" literal="0" length="1" padding="0" justify="right" />
			   			<field name="separate2" length="9" padding=" "  />
			   			<field name="batchNum" literal="01" length="2" />
			   			<field name="env" literal="LIVE" length="4" />
			   		</record>
			   		<record name="detail" class="map" occurs="0+" order="2" >
			   			<field name="payMethod" literal="2" length="1" padding="0" justify="right" />
			   			<field name="compCode" length="4" />
			   			<field name="branchNum" length="6" padding="0" justify="right" />
			   			<field name="empNum" length="7" padding="0" justify="right" />
			   			<field name="accNum" length="19" padding="0" justify="right" />
			   			<field name="separate1" literal=" " length="1" />
			   			<field name="accType" literal="1" length="1" />
			   			<field name="amt" type="java.math.BigDecimal" length="11" padding="0" justify="right" />
			   			<field name="accName" length="20" />
			   			<field name="separate2" length="10" padding=" " />
			   			<field name="stmRef" length="15" padding=" " />
			   			<field name="rtgsInd" literal=" " length="1" />
			   		</record>
			   		<record name="trailer" class="map" occurs="1" order="3" >
			   			<field name="errFlag" literal="2" length="1" />
			   			<field name="compCode" length="4" />
			   			<field name="trailerInd" literal="T" length="1" />
			   			<field name="separate1" length="30" padding=" " />
			   			<field name="amt" type="java.math.BigDecimal" padding="0" justify="right" length="13" />
			   			<field name="separate2" length="14" padding=" " />
			   			<field name="numTrans" padding="0" justify="right" length="7" />
			   			<field name="separate2" length="26" padding=" " />
			   		</record>
			   	</stream>
			</beanio>
			""";
	
	@Override
	public InputStream getEftMapping() {
		// can improve to get mapping from attachment
        return IOUtils.toInputStream(eftSBDMappingContent, StandardCharsets.UTF_8);
	}
	
	@Override
	public String getStreamMappingName() {
		return "eftStandardBankDomestic";
	}
	
	public Map<String, Object> buildEftSbdHeader(MPaySelectionCheck[] checks, boolean depositBatch, String paymentRule, StringBuffer err) {
		Map<String, Object> eftSBDHeader = new HashMap<>();
		eftSBDHeader.put("compCode", "592C");
		eftSBDHeader.put("compName", "MQA Operations");
		eftSBDHeader.put("actDate", "20250110");
		eftSBDHeader.put("stmRef", "MQA            ");
		
		return eftSBDHeader;
	}

	public List<Map<String, Object>> buildEftSbdDetail(MPaySelectionCheck[] checks, boolean depositBatch, String paymentRule, StringBuffer err) {
		List<Map<String, Object>> eftSbdDetailData = new ArrayList<>();
		return eftSbdDetailData;
		
	}
	
	public Map<String, Object> buildEftSbdTrailer(Map<String, Object> eftSBDHeader, List<Map<String, Object>> eftSbdDetailData) {
		Map<String, Object> eftSBDTrailer = new HashMap<>();
		eftSBDTrailer.put("compCode", eftSBDHeader.get("compCode"));
		
		final BigDecimal amount = new BigDecimal(0);
		eftSbdDetailData.forEach(eftSBDDetailLine -> {
			amount.add((BigDecimal)eftSBDDetailLine.get("amt"));
		});
		eftSBDTrailer.put("amt", amount);
		
		eftSBDTrailer.put("numTrans", eftSbdDetailData.size());
		
		return eftSBDTrailer;
	}

	@Override
	public Iterator<Entry<String, Map<String, Object>>> getLineIterator(MPaySelectionCheck[] checks, boolean depositBatch, String paymentRule, StringBuffer err) {
		
		Map<String, Object>  eftSBDHeader = buildEftSbdHeader(checks, depositBatch, paymentRule, err);
		List<Map<String, Object>> eftSbdDetailData = buildEftSbdDetail(checks, depositBatch, paymentRule, err);
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
