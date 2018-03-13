package fi.trustnet.example.issuer;

import java.io.File;

import org.hyperledger.indy.sdk.IndyConstants;
import org.hyperledger.indy.sdk.LibIndy;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidJSONParameters.CreateAndStoreMyDidJSONParameter;
import org.hyperledger.indy.sdk.did.DidResults.CreateAndStoreMyDidResult;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.pool.PoolJSONParameters.OpenPoolLedgerJSONParameter;
import org.hyperledger.indy.sdk.wallet.Wallet;

public class Sovrin {

	public static final String TRUSTEE_DID = "V4SGRU86Z58d6TV7PBUe6f";
	public static final String TRUSTEE_VERKEY = "GJ1SzoWzavQYfNL9XkaJdrQejfztN4XqdsiV4ct3LXKL";
	public static final String TRUSTEE_SEED = "000000000000000000000000Trustee1";

	static String createDid(String userSeed) throws Exception {

		if (! LibIndy.isInitialized()) LibIndy.init(new File("./lib/libindy.so"));

		// open pool and wallets

		OpenPoolLedgerJSONParameter openPoolLedgerJSONParameter = new OpenPoolLedgerJSONParameter(Boolean.TRUE, null, null);
		Pool pool = Pool.openPoolLedger("11347-04", openPoolLedgerJSONParameter.toJson()).get();

		Wallet walletTrustee = Wallet.openWallet("trusteewallet", null, null).get();
		Wallet walletUser = Wallet.openWallet("userwallet", null, null).get();

		// create TRUSTEE DID

		CreateAndStoreMyDidJSONParameter createAndStoreMyDidJSONParameterTrustee = new CreateAndStoreMyDidJSONParameter(null, TRUSTEE_SEED, null, null);
		Did.createAndStoreMyDid(walletTrustee, createAndStoreMyDidJSONParameterTrustee.toJson()).get();

		// create USER DID

		CreateAndStoreMyDidJSONParameter createAndStoreMyDidJSONParameter = new CreateAndStoreMyDidJSONParameter(null, userSeed, null, null);
		CreateAndStoreMyDidResult createAndStoreMyDidResult = Did.createAndStoreMyDid(walletUser, createAndStoreMyDidJSONParameter.toJson()).get();

		String userDid = createAndStoreMyDidResult.getDid();
		String userVerkey = createAndStoreMyDidResult.getVerkey();

		// create NYM request

		String nymRequest = Ledger.buildNymRequest(TRUSTEE_DID, userDid, userVerkey, /*"{\"alias\":\"b\"}"*/ null, IndyConstants.ROLE_TRUSTEE).get();
		Ledger.signAndSubmitRequest(pool, walletTrustee, TRUSTEE_DID, nymRequest).get();

		// close wallets

		walletTrustee.closeWallet().get();
		walletUser.closeWallet().get();
		pool.closePoolLedger().get();

		// done

		return "did:sov:" + userDid;
	}

	static String createDid() throws Exception {

		return createDid(null);
	}
}
