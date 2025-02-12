/******************************************************************************
 * Product: iDempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2012 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
/** Generated Model - DO NOT CHANGE */
package za.co.ntier.bank.payment.export.model;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;
import org.compiere.model.*;

/** Generated Model for ZZ_BankCode
 *  @author iDempiere (generated)
 *  @version Release 12 - $Id$ */
@org.adempiere.base.Model(table="ZZ_BankCode")
public class X_ZZ_BankCode extends PO implements I_ZZ_BankCode, I_Persistent
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20250212L;

    /** Standard Constructor */
    public X_ZZ_BankCode (Properties ctx, int ZZ_BankCode_ID, String trxName)
    {
      super (ctx, ZZ_BankCode_ID, trxName);
      /** if (ZZ_BankCode_ID == 0)
        {
			setZZ_BankCode_ID (0);
        } */
    }

    /** Standard Constructor */
    public X_ZZ_BankCode (Properties ctx, int ZZ_BankCode_ID, String trxName, String ... virtualColumns)
    {
      super (ctx, ZZ_BankCode_ID, trxName, virtualColumns);
      /** if (ZZ_BankCode_ID == 0)
        {
			setZZ_BankCode_ID (0);
        } */
    }

    /** Standard Constructor */
    public X_ZZ_BankCode (Properties ctx, String ZZ_BankCode_UU, String trxName)
    {
      super (ctx, ZZ_BankCode_UU, trxName);
      /** if (ZZ_BankCode_UU == null)
        {
			setZZ_BankCode_ID (0);
        } */
    }

    /** Standard Constructor */
    public X_ZZ_BankCode (Properties ctx, String ZZ_BankCode_UU, String trxName, String ... virtualColumns)
    {
      super (ctx, ZZ_BankCode_UU, trxName, virtualColumns);
      /** if (ZZ_BankCode_UU == null)
        {
			setZZ_BankCode_ID (0);
        } */
    }

    /** Load Constructor */
    public X_ZZ_BankCode (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 3 - Client - Org
      */
    protected int get_AccessLevel()
    {
      return accessLevel.intValue();
    }

    /** Load Meta Data */
    protected POInfo initPO (Properties ctx)
    {
      POInfo poi = POInfo.getPOInfo (ctx, Table_ID, get_TrxName());
      return poi;
    }

    public String toString()
    {
      StringBuilder sb = new StringBuilder ("X_ZZ_BankCode[")
        .append(get_ID()).append(",Name=").append(getName()).append("]");
      return sb.toString();
    }

	public org.compiere.model.I_C_BankAccount getC_BankAccount() throws RuntimeException
	{
		return (org.compiere.model.I_C_BankAccount)MTable.get(getCtx(), org.compiere.model.I_C_BankAccount.Table_ID)
			.getPO(getC_BankAccount_ID(), get_TrxName());
	}

	/** Set Bank Account.
		@param C_BankAccount_ID Account at the Bank
	*/
	public void setC_BankAccount_ID (int C_BankAccount_ID)
	{
		if (C_BankAccount_ID < 1)
			set_Value (COLUMNNAME_C_BankAccount_ID, null);
		else
			set_Value (COLUMNNAME_C_BankAccount_ID, Integer.valueOf(C_BankAccount_ID));
	}

	/** Get Bank Account.
		@return Account at the Bank
	  */
	public int getC_BankAccount_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_BankAccount_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Date Last Action.
		@param DateLastAction Date this request was last acted on
	*/
	public void setDateLastAction (Timestamp DateLastAction)
	{
		set_ValueNoCheck (COLUMNNAME_DateLastAction, DateLastAction);
	}

	/** Get Date Last Action.
		@return Date this request was last acted on
	  */
	public Timestamp getDateLastAction()
	{
		return (Timestamp)get_Value(COLUMNNAME_DateLastAction);
	}

	/** Set Name.
		@param Name Alphanumeric identifier of the entity
	*/
	public void setName (String Name)
	{
		set_Value (COLUMNNAME_Name, Name);
	}

	/** Get Name.
		@return Alphanumeric identifier of the entity
	  */
	public String getName()
	{
		return (String)get_Value(COLUMNNAME_Name);
	}

	/** Set Search Key.
		@param Value Search key for the record in the format required - must be unique
	*/
	public void setValue (String Value)
	{
		set_Value (COLUMNNAME_Value, Value);
	}

	/** Get Search Key.
		@return Search key for the record in the format required - must be unique
	  */
	public String getValue()
	{
		return (String)get_Value(COLUMNNAME_Value);
	}

	/** Set Bank Code.
		@param ZZ_BankCode_ID Add extra info to bank account for generate ETF
	*/
	public void setZZ_BankCode_ID (int ZZ_BankCode_ID)
	{
		if (ZZ_BankCode_ID < 1)
			set_ValueNoCheck (COLUMNNAME_ZZ_BankCode_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_ZZ_BankCode_ID, Integer.valueOf(ZZ_BankCode_ID));
	}

	/** Get Bank Code.
		@return Add extra info to bank account for generate ETF
	  */
	public int getZZ_BankCode_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_ZZ_BankCode_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set ZZ_BankCode_UU.
		@param ZZ_BankCode_UU ZZ_BankCode_UU
	*/
	public void setZZ_BankCode_UU (String ZZ_BankCode_UU)
	{
		set_Value (COLUMNNAME_ZZ_BankCode_UU, ZZ_BankCode_UU);
	}

	/** Get ZZ_BankCode_UU.
		@return ZZ_BankCode_UU	  */
	public String getZZ_BankCode_UU()
	{
		return (String)get_Value(COLUMNNAME_ZZ_BankCode_UU);
	}
}