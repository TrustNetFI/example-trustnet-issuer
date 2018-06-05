package fi.trustnet.example.issuer;
import org.hyperledger.indy.sdk.IndyConstants;
import org.hyperledger.indy.sdk.LibIndy;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidJSONParameters.CreateAndStoreMyDidJSONParameter;
import org.hyperledger.indy.sdk.did.DidResults.CreateAndStoreMyDidResult;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.pool.PoolJSONParameters.CreatePoolLedgerConfigJSONParameter;
import org.hyperledger.indy.sdk.pool.PoolJSONParameters.OpenPoolLedgerJSONParameter;
import org.hyperledger.indy.sdk.wallet.Wallet;

public class Sovrin {

	public static final String TRUSTEE_DID = "V4SGRU86Z58d6TV7PBUe6f";
	public static final String TRUSTEE_VERKEY = "GJ1SzoWzavQYfNL9XkaJdrQejfztN4XqdsiV4ct3LXKL";
	public static final String TRUSTEE_SEED = "000000000000000000000000Trustee1";

	public static Pool pool;
	public static Wallet walletTrustee;
	public static Wallet walletIssuer;

	public static void open() throws Exception {

		if (! LibIndy.isInitialized()) LibIndy.init("./lib/");

		// create pool config and wallets

		CreatePoolLedgerConfigJSONParameter createPoolLedgerConfigJSONParameter = new CreatePoolLedgerConfigJSONParameter("11347-04.txn");
		Pool.createPoolLedgerConfig("11347-04", createPoolLedgerConfigJSONParameter.toJson()).get();

		Wallet.createWallet("11347-04", "trusteewallet", "default", null, null).get();
		Wallet.createWallet("11347-04", "issuerwallet", "default", null, null).get();

		// open pool and wallets

		OpenPoolLedgerJSONParameter openPoolLedgerJSONParameter = new OpenPoolLedgerJSONParameter(Boolean.TRUE, null, null);
		pool = Pool.openPoolLedger("11347-04", openPoolLedgerJSONParameter.toJson()).get();

		walletTrustee = Wallet.openWallet("trusteewallet", null, null).get();
		walletIssuer = Wallet.openWallet("issuerwallet", null, null).get();
	}

	public static CreateAndStoreMyDidResult createDid() throws Exception {

		return createDid(null);
	}

	public static CreateAndStoreMyDidResult createDid(String seed) throws Exception {

		// create TRUSTEE DID

		CreateAndStoreMyDidJSONParameter createAndStoreMyDidJSONParameterTrustee = new CreateAndStoreMyDidJSONParameter(null, TRUSTEE_SEED, null, null);
		Did.createAndStoreMyDid(walletTrustee, createAndStoreMyDidJSONParameterTrustee.toJson()).get();

		// create ISSUER DID

		CreateAndStoreMyDidJSONParameter createAndStoreMyDidJSONParameter = new CreateAndStoreMyDidJSONParameter(null, seed, null, null);
		CreateAndStoreMyDidResult createAndStoreMyDidResult = Did.createAndStoreMyDid(walletIssuer, createAndStoreMyDidJSONParameter.toJson()).get();

		String issuerDid = createAndStoreMyDidResult.getDid();
		String issuerVerkey = createAndStoreMyDidResult.getVerkey();

		String nymRequest = Ledger.buildNymRequest(TRUSTEE_DID, issuerDid, issuerVerkey, /*"{\"alias\":\"b\"}"*/ null, IndyConstants.ROLE_TRUSTEE).get();
		Ledger.signAndSubmitRequest(pool, walletTrustee, TRUSTEE_DID, nymRequest).get();

		return createAndStoreMyDidResult;
	}

	static void close() throws Exception {

		// close wallets and pool

		walletTrustee.closeWallet().get();
		walletIssuer.closeWallet().get();

		pool.closePoolLedger().get();
	}
}
