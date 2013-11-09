package com.ripple.core;

import com.ripple.core.enums.LedgerEntryType;
import com.ripple.core.enums.TransactionEngineResult;
import com.ripple.core.fields.Field;
import com.ripple.core.fields.Type;
import com.ripple.core.formats.TxFormat;
import com.ripple.core.serialized.BinarySerializer;
import com.ripple.core.serialized.SerializedType;
import com.ripple.core.serialized.TypeTranslator;
import com.ripple.core.types.*;
import com.ripple.core.types.hash.Hash256;
import com.ripple.core.types.translators.Translators;
import com.ripple.core.types.uint.UInt16;
import com.ripple.core.types.uint.UInt32;
import com.ripple.core.types.uint.UInt64;
import com.ripple.core.types.uint.UInt8;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class STObjectTest {
    @Test
    public void testNestedObjectSerialization() throws Exception {
        String rippleLibHex =    "120007220000000024000195F964400000170A53AC2065D5460561EC9DE000000000000000000000000000" +
                                "494C53000000000092D705968936C419CE614BF264B5EEB1CEA47FF468400000000000000A7321028472865" +
                                "AF4CB32AA285834B57576B7290AA8C31B459047DB27E16F418D6A71667447304502202ABE08D5E78D1E74A4" +
                                "C18F2714F64E87B8BD57444AFA5733109EB3C077077520022100DB335EE97386E4C0591CAC024D50E9230D8" +
                                "F171EEB901B5E5E4BD6D1E0AEF98C811439408A69F0895E62149CFCC006FB89FA7D1E6E5D";


        String rippledHex = "120007220000000024000195F964400000170A53AC2065D5460561EC9DE000000000000000000000000000494C53000000000092D705968936C419CE614BF264B5EEB1CEA47FF468400000000000000A7321028472865AF4CB32AA285834B57576B7290AA8C31B459047DB27E16F418D6A71667447304502202ABE08D5E78D1E74A4C18F2714F64E87B8BD57444AFA5733109EB3C077077520022100DB335EE97386E4C0591CAC024D50E9230D8F171EEB901B5E5E4BD6D1E0AEF98C811439408A69F0895E62149CFCC006FB89FA7D1E6E5D";

        String json = "{" +
                "  \"Account\": \"raD5qJMAShLeHZXf9wjUmo6vRK4arj9cF3\"," +
                "  \"Fee\": \"10\"," +
                "  \"Flags\": 0," +
                "  \"Sequence\": 103929," +
                "  \"SigningPubKey\": \"028472865AF4CB32AA285834B57576B7290AA8C31B459047DB27E16F418D6A7166\"," +
                "  \"TakerGets\": {" +
                "    \"currency\": \"ILS\"," +
                "    \"issuer\": \"rNPRNzBB92BVpAhhZr4iXDTveCgV5Pofm9\"," +
                "    \"value\": \"1694.768\"" +
                "  }," +
                "  \"TakerPays\": \"98957503520\"," +
                "  \"TransactionType\": \"OfferCreate\"," +
                "  \"TxnSignature\": \"304502202ABE08D5E78D1E74A4C18F2714F64E87B8BD57444AFA5733109EB3C077077520022100DB335EE97386E4C0591CAC024D50E9230D8F171EEB901B5E5E4BD6D1E0AEF98C\"," +
                "  \"hash\": \"232E91912789EA1419679A4AA920C22CFC7C6B601751D6CBE89898C26D7F4394\"," +
                "  \"metaData\": {" +
                "    \"AffectedNodes\": [" +
                "      {" +
                "        \"CreatedNode\": {" +
                "          \"LedgerEntryType\": \"Offer\"," +
                "          \"LedgerIndex\": \"3596CE72C902BAFAAB56CC486ACAF9B4AFC67CF7CADBB81A4AA9CBDC8C5CB1AA\"," +
                "          \"NewFields\": {" +
                "            \"Account\": \"raD5qJMAShLeHZXf9wjUmo6vRK4arj9cF3\"," +
                "            \"BookDirectory\": \"62A3338CAF2E1BEE510FC33DE1863C56948E962CCE173CA55C14BE8A20D7F000\"," +
                "            \"OwnerNode\": \"000000000000000E\"," +
                "            \"Sequence\": 103929," +
                "            \"TakerGets\": {" +
                "              \"currency\": \"ILS\"," +
                "              \"issuer\": \"rNPRNzBB92BVpAhhZr4iXDTveCgV5Pofm9\"," +
                "              \"value\": \"1694.768\"" +
                "            }," +
                "            \"TakerPays\": \"98957503520\"" +
                "          }" +
                "        }" +
                "      }," +
                "      {" +
                "        \"CreatedNode\": {" +
                "          \"LedgerEntryType\": \"DirectoryNode\"," +
                "          \"LedgerIndex\": \"62A3338CAF2E1BEE510FC33DE1863C56948E962CCE173CA55C14BE8A20D7F000\"," +
                "          \"NewFields\": {" +
                "            \"ExchangeRate\": \"5C14BE8A20D7F000\"," +
                "            \"RootIndex\": \"62A3338CAF2E1BEE510FC33DE1863C56948E962CCE173CA55C14BE8A20D7F000\"," +
                "            \"TakerGetsCurrency\": \"000000000000000000000000494C530000000000\"," +
                "            \"TakerGetsIssuer\": \"92D705968936C419CE614BF264B5EEB1CEA47FF4\"" +
                "          }" +
                "        }" +
                "      }," +
                "      {" +
                "        \"ModifiedNode\": {" +
                "          \"FinalFields\": {" +
                "            \"Flags\": 0," +
                "            \"IndexPrevious\": \"0000000000000000\"," +
                "            \"Owner\": \"raD5qJMAShLeHZXf9wjUmo6vRK4arj9cF3\"," +
                "            \"RootIndex\": \"801C5AFB5862D4666D0DF8E5BE1385DC9B421ED09A4269542A07BC0267584B64\"" +
                "          }," +
                "          \"LedgerEntryType\": \"DirectoryNode\"," +
                "          \"LedgerIndex\": \"AB03F8AA02FFA4635E7CE2850416AEC5542910A2B4DBE93C318FEB08375E0DB5\"" +
                "        }" +
                "      }," +
                "      {" +
                "        \"ModifiedNode\": {" +
                "          \"FinalFields\": {" +
                "            \"Account\": \"raD5qJMAShLeHZXf9wjUmo6vRK4arj9cF3\"," +
                "            \"Balance\": \"106861218302\"," +
                "            \"Flags\": 0," +
                "            \"OwnerCount\": 9," +
                "            \"Sequence\": 103930" +
                "          }," +
                "          \"LedgerEntryType\": \"AccountRoot\"," +
                "          \"LedgerIndex\": \"CF23A37E39A571A0F22EC3E97EB0169936B520C3088963F16C5EE4AC59130B1B\"," +
                "          \"PreviousFields\": {" +
                "            \"Balance\": \"106861218312\"," +
                "            \"OwnerCount\": 8," +
                "            \"Sequence\": 103929" +
                "          }," +
                "          \"PreviousTxnID\": \"DE15F43F4A73C4F6CB1C334D9E47BDE84467C0902796BB81D4924885D1C11E6D\"," +
                "          \"PreviousTxnLgrSeq\": 3225338" +
                "        }" +
                "      }" +
                "    ]," +
                "    \"TransactionIndex\": 0," +
                "    \"TransactionResult\": \"tesSUCCESS\"" +
                "  }" +
                "}";

        JSONObject tx = new JSONObject(json);
        STObject meta = STObject.fromJSONObject((JSONObject) tx.remove("metaData"));
        STObject stObject = STObject.fromJSONObject(tx);

        String rippledMetaHex = "201C00000000F8E311006F563596CE72C902BAFAAB56CC486ACAF9B4AFC67CF7CADBB81A4AA9CBDC8C5CB1AAE824000195F934000000000000000E501062A3338CAF2E1BEE510FC33DE1863C56948E962CCE173CA55C14BE8A20D7F00064400000170A53AC2065D5460561EC9DE000000000000000000000000000494C53000000000092D705968936C419CE614BF264B5EEB1CEA47FF4811439408A69F0895E62149CFCC006FB89FA7D1E6E5DE1E1E31100645662A3338CAF2E1BEE510FC33DE1863C56948E962CCE173CA55C14BE8A20D7F000E8365C14BE8A20D7F0005862A3338CAF2E1BEE510FC33DE1863C56948E962CCE173CA55C14BE8A20D7F0000311000000000000000000000000494C530000000000041192D705968936C419CE614BF264B5EEB1CEA47FF4E1E1E511006456AB03F8AA02FFA4635E7CE2850416AEC5542910A2B4DBE93C318FEB08375E0DB5E7220000000032000000000000000058801C5AFB5862D4666D0DF8E5BE1385DC9B421ED09A4269542A07BC0267584B64821439408A69F0895E62149CFCC006FB89FA7D1E6E5DE1E1E511006125003136FA55DE15F43F4A73C4F6CB1C334D9E47BDE84467C0902796BB81D4924885D1C11E6D56CF23A37E39A571A0F22EC3E97EB0169936B520C3088963F16C5EE4AC59130B1BE624000195F92D000000086240000018E16CCA08E1E7220000000024000195FA2D000000096240000018E16CC9FE811439408A69F0895E62149CFCC006FB89FA7D1E6E5DE1E1F1031000";


//        Amount amount = stObject.get(Amount.TakerGets);
//        String hex = Amount.translate.toHex(amount);


        String actual = stObject.toHex();

        assertEquals(rippledHex, rippleLibHex);
        assertEquals(rippledHex, actual);

        debugObject(rippledMetaHex, meta, 0);


        System.out.println(rippledMetaHex);
        assertEquals(rippledMetaHex.length(), meta.toHex().length());
        assertEquals(rippledMetaHex, meta.toHex());
    }

    private void debugObject(String patterns, STObject meta, int starting) {
        for (Field field : meta) {
            System.out.println("Field " + field +  " is of type " + field.getType());

            if (!field.isSerialized()) {
                continue;
            }

            SerializedType serializedType = meta.get(field);
            TypeTranslator<SerializedType> tr = Translators.forField(field);

            BinarySerializer bn = new BinarySerializer();
            bn.add(field, serializedType);
            String hex = Hex.toHexString(bn.toByteArray());

            if (field.getType() == Type.ARRAY) {
//                System.out.println("Entering array" + field);

                STArray array = (STArray) serializedType;
                for (STObject stObject : array) {
                    debugObject(patterns, stObject, starting);
                }
            } else if (field.getType() == Type.OBJECT) {
//                System.out.println("Entering object" + field);
                debugObject(patterns, (STObject) serializedType, starting);
            } else {
                int ix = patterns.indexOf(hex.toUpperCase(), starting);
                System.out.println(field + " (" + ix + "," + (ix + hex.length()) + ")");

                starting = (ix + hex.length());

                boolean contains = ix != 1;
                assertTrue(contains);
//                if (!contains && field != Field.hash) {
////                    System.out.println("Can't find " + field);
//                }

//                System.out.println(field.getType() + " " + contains + " " + field + " " +/*+ header +*/ hex);


            }
        }
    }

    @Test
    public void testTypeInference() {

        STObject so = STObject.newInstance();
        so.put(Field.valueOf("LowLimit"), "10.0/USD");
        so.put(Amount.Balance, "125.0");

        assertEquals(so.get(Amount.Balance).toDropsString(), "125000000");
        assertEquals(so.get(Amount.LowLimit).currencyString(), "USD");

        assertNotNull(so.get(Amount.LowLimit));
        assertNull(so.get(Amount.HighLimit));
    }


    @Test
    /**
     * We just testing this won't blow up due to unknown `date` field!
     */
    public void testfromJSONObjectWithUnknownFields() throws JSONException {

        String json = "{\"date\": 434707820,\n" +
                "\"hash\": \"66347806574036FD3D3E9FDA20A411FA8B2D26AA3C3725A107FCF0050F1E4B86\"}";

        STObject so = STObject.fromJSONObject(new JSONObject(json));
    }

    String metaString = "{\"AffectedNodes\": [{\"ModifiedNode\": {\"FinalFields\": {\"Account\": \"rwMyB1diFJ7xqEKYGYgk9tKrforvTr33M5\","+
            "\"Balance\": \"286000447\","+
            "\"Flags\": 0,"+
            "\"OwnerCount\": 4,"+
            "\"Sequence\": 35},"+
            "\"LedgerEntryType\": \"AccountRoot\","+
            "\"LedgerIndex\": \"32FE2333B117B257F3AB58E1CB15A6533DC27FDD61FEB1027858D367B40B559A\","+
            "\"PreviousFields\": {\"Balance\": \"286000463\","+
            "\"Sequence\": 34},"+
            "\"PreviousTxnID\": \"33562B82489F263F173801272D02178C0018A40ACFDC84B59976CE7C163F41FC\","+
            "\"PreviousTxnLgrSeq\": 2681281}},"+
            "{\"ModifiedNode\": {\"FinalFields\": {\"Account\": \"rP1coskQzayaQ9geMdJgAV5f3tNZcHghzH\","+
            "\"Balance\": \"99249214171\","+
            "\"Flags\": 0,"+
            "\"OwnerCount\": 3,"+
            "\"Sequence\": 177},"+
            "\"LedgerEntryType\": \"AccountRoot\","+
            "\"LedgerIndex\": \"D66D0EC951FD5707633BEBE74DB18B6D2DDA6771BA0FBF079AD08BFDE6066056\","+
            "\"PreviousFields\": {\"Balance\": \"99249214170\"},"+
            "\"PreviousTxnID\": \"33562B82489F263F173801272D02178C0018A40ACFDC84B59976CE7C163F41FC\","+
            "\"PreviousTxnLgrSeq\": 2681281}}],"+
            "\"TransactionIndex\": 2,"+
            "\"TransactionResult\": \"tesSUCCESS\"}";

    @Test
    public void test_parsing_transaction_meta_with_STArray() throws Exception {
        STObject meta = STObject.fromJSONObject(new JSONObject(metaString));
        STArray nodes = meta.get(STArray.AffectedNodes);

        // Some helper methods to get enum fields
        assertEquals(TransactionEngineResult.tesSUCCESS,
                     meta.transactionResult());

        STObject firstAffected = nodes.get(0);
        assertEquals(LedgerEntryType.AccountRoot,
                     firstAffected.get(STObject.ModifiedNode).ledgerEntryType());

        assertTrue(firstAffected.has(STObject.ModifiedNode));
        assertEquals(new UInt32(35),  finalSequence(firstAffected));
        assertEquals(new UInt32(177), finalSequence(nodes.get(1)));
    }

    private UInt32 finalSequence(STObject affected) {
        return affected.get(STObject.ModifiedNode).get(STObject.FinalFields).get(UInt32.Sequence);
    }

    @Test
    public void testSerializedPaymentTransaction() throws JSONException {
        String expectedSerialization = "120000240000000561D4C44364C5BB00000000000000000000000000005553440000000000B5F762798A53D543A014CAF8B297CFF8F2F937E868400000000000000F73210330E7FC9D56BB25D6893BA3F317AE5BCF33B3291BD63DB32654A313222F7FD0208114B5F762798A53D543A014CAF8B297CFF8F2F937E88314FD94A75318DE40B1D513E6764ECBCB6F1E7056ED";

        AccountID ac = AccountID.fromSeedString(TestFixtures.master_seed);
        STObject fromSO = STObject.newInstance();

        fromSO.put(Field.TransactionType, "Payment");
        fromSO.put(AccountID.Account, ac.address);
        fromSO.put(UInt32.Sequence, 5);
        fromSO.put(Amount.Fee, "15");
        fromSO.put(VariableLength.SigningPubKey, ac.getKeyPair().pubHex());
        fromSO.put(AccountID.Destination, TestFixtures.bob_account.address);
        fromSO.put(Amount.Amount, "12/USD/" + ac.address);

        assertEquals(expectedSerialization, fromSO.toHex());
    }

    @Test
    public void testSerializedPaymentTransactionFromJSON() throws JSONException {
        String tx_json = "{\"Amount\":{\"issuer\":\"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\"," +
                                      "\"value\":\"12\"," +
                                      "\"currency\":\"USD\"}," +
                          "\"Fee\":\"15\"," +
                          "\"SigningPubKey\":\"0330e7fc9d56bb25d6893ba3f317ae5bcf33b3291bd63db32654a313222f7fd020\"," +
                          "\"Account\":\"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\"," +
                          "\"TransactionType\":\"Payment\"," +
                          "\"Sequence\":5," +
                          "\"Destination\":\"rQfFsw6w4wdymTCSfF2fZQv7SZzfGyzsyB\"}";

        String expectedSerialization = "120000240000000561D4C44364C5BB000000000000000000000000000055534" +
                                       "40000000000B5F762798A53D543A014CAF8B297CFF8F2F937E8684000000000" +
                                       "00000F73210330E7FC9D56BB25D6893BA3F317AE5BCF33B3291BD63DB32654A" +
                                       "313222F7FD0208114B5F762798A53D543A014CAF8B297CFF8F2F937E88314FD" +
                                       "94A75318DE40B1D513E6764ECBCB6F1E7056ED";

        STObject fromJSON = STObject.fromJSONObject(new JSONObject(tx_json));
        assertEquals(expectedSerialization, fromJSON.toHex());
    }

    @Test
    public void testUINT() throws JSONException {

        JSONObject json = new JSONObject("{\"Expiration\" : 21}");
        STObject so = STObject.translate.fromJSONObject(json);
        assertEquals(21, so.get(UInt32.Expiration).longValue());

        byte[] bytes =  UInt8.translate.toWireBytes(new UInt8(1));
        byte[] bytes2 = UInt16.translate.toWireBytes(new UInt16(1));
        byte[] bytes4 = UInt32.translate.toWireBytes(new UInt32(1));
        byte[] bytes8 = UInt64.translate.toWireBytes(new UInt64(1));

        assertEquals( bytes.length, 1);
        assertEquals(bytes2.length, 2);
        assertEquals(bytes4.length, 4);
        assertEquals(bytes8.length, 8);
    }

    @Test
    public void testSymbolics() throws JSONException {
        assertNotNull(TxFormat.fromString("Payment"));

        JSONObject json = new JSONObject("{\"Expiration\"        : 21, " +
                                          "\"TransactionResult\" : 0,  " +
                                          "\"TransactionType\"   : 0  }");

        STObject so = STObject.translate.fromJSONObject(json);
        assertEquals(so.getFormat(), TxFormat.Payment);
        so.setFormat(null); // Else it (SHOULD) attempt to validate something clearly unFormatted

        JSONObject object = STObject.translate.toJSONObject(so);

        assertEquals(object.get("TransactionResult"), "tesSUCCESS");
        assertEquals(object.get("TransactionType"), "Payment");

    }
}
