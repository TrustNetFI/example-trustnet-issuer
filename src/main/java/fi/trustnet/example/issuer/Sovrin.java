package fi.trustnet.example.issuer;
import java.util.concurrent.ExecutionException;

import org.hyperledger.indy.sdk.IndyConstants;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.LibIndy;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidAlreadyExistsException;
import org.hyperledger.indy.sdk.did.DidJSONParameters.CreateAndStoreMyDidJSONParameter;
import org.hyperledger.indy.sdk.did.DidResults.CreateAndStoreMyDidResult;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.pool.PoolJSONParameters.CreatePoolLedgerConfigJSONParameter;
import org.hyperledger.indy.sdk.pool.PoolJSONParameters.OpenPoolLedgerJSONParameter;
import org.hyperledger.indy.sdk.pool.PoolLedgerConfigExistsException;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.hyperledger.indy.sdk.wallet.WalletExistsException;

public class Sovrin {

	public static final String TRUSTEE_DID = "V4SGRU86Z58d6TV7PBUe6f";
	public static final String TRUSTEE_VERKEY = "GJ1SzoWzavQYfNL9XkaJdrQejfztN4XqdsiV4ct3LXKL";
	public static final String TRUSTEE_SEED = "000000000000000000000000Trustee1";

	public static final String WALLET_CONFIG_TRUSTEE = "{ \"id\":\"" + "trusteewallet" + "\", \"storage_type\":\"" + "default" + "\"}";
	public static final String WALLET_CONFIG_ISSUER = "{ \"id\":\"" + "issuerwallet" + "\", \"storage_type\":\"" + "default" + "\"}";
	public static final String WALLET_CREDENTIALS = "{ \"key\":\"key\" }";

	public static Pool pool;
	public static Wallet walletTrustee;
	public static Wallet walletIssuer;

	public static void open() throws Exception {

		if (! LibIndy.isInitialized()) LibIndy.init("./lib/");
		Pool.setProtocolVersion(2);

		// create pool config and wallets

		try {

			CreatePoolLedgerConfigJSONParameter createPoolLedgerConfigJSONParameter = new CreatePoolLedgerConfigJSONParameter("danube.txn");
			Pool.createPoolLedgerConfig("danube", createPoolLedgerConfigJSONParameter.toJson()).get();
		} catch (IndyException | InterruptedException | ExecutionException ex) {

			IndyException iex = null;
			if (ex instanceof IndyException) iex = (IndyException) ex;
			if (ex instanceof ExecutionException && ex.getCause() instanceof IndyException) iex = (IndyException) ex.getCause();
			if (iex instanceof PoolLedgerConfigExistsException) {

				System.err.println("Pool config \"danube\" has already been created.");
			} else {

				throw new RuntimeException("Cannot create pool config \"danube\": " + ex.getMessage(), ex);
			}
		}

		try {

			Wallet.createWallet(WALLET_CONFIG_TRUSTEE, WALLET_CREDENTIALS).get();
		} catch (IndyException | InterruptedException | ExecutionException ex) {

			IndyException iex = null;
			if (ex instanceof IndyException) iex = (IndyException) ex;
			if (ex instanceof ExecutionException && ex.getCause() instanceof IndyException) iex = (IndyException) ex.getCause();
			if (iex instanceof WalletExistsException) {

				System.err.println("Wallet " + WALLET_CONFIG_TRUSTEE + " has already been created.");
			} else {

				throw new RuntimeException("Cannot create wallet " + WALLET_CONFIG_TRUSTEE + ": " + ex.getMessage(), ex);
			}
		}
		try {

			Wallet.createWallet(WALLET_CONFIG_ISSUER, WALLET_CREDENTIALS).get();
		} catch (IndyException | InterruptedException | ExecutionException ex) {

			IndyException iex = null;
			if (ex instanceof IndyException) iex = (IndyException) ex;
			if (ex instanceof ExecutionException && ex.getCause() instanceof IndyException) iex = (IndyException) ex.getCause();
			if (iex instanceof WalletExistsException) {

				System.err.println("Wallet " + WALLET_CONFIG_ISSUER + " has already been created.");
			} else {

				throw new RuntimeException("Cannot create wallet " + WALLET_CONFIG_ISSUER + ": " + ex.getMessage(), ex);
			}
		}

		// open pool and wallets

		OpenPoolLedgerJSONParameter openPoolLedgerJSONParameter = new OpenPoolLedgerJSONParameter(null, null);
		pool = Pool.openPoolLedger("danube", openPoolLedgerJSONParameter.toJson()).get();

		walletTrustee = Wallet.openWallet(WALLET_CONFIG_TRUSTEE, WALLET_CREDENTIALS).get();
		walletIssuer = Wallet.openWallet(WALLET_CONFIG_ISSUER, WALLET_CREDENTIALS).get();
	}

	public static CreateAndStoreMyDidResult createDid() throws Exception {

		return createDid(null);
	}

	public static CreateAndStoreMyDidResult createDid(String seed) throws Exception {

		// create TRUSTEE DID

		CreateAndStoreMyDidJSONParameter createAndStoreMyDidJSONParameterTrustee = new CreateAndStoreMyDidJSONParameter(null, TRUSTEE_SEED, null, null);

		try {

			Did.createAndStoreMyDid(walletTrustee, createAndStoreMyDidJSONParameterTrustee.toJson()).get();
		} catch (IndyException | InterruptedException | ExecutionException ex) {

			IndyException iex = null;
			if (ex instanceof IndyException) iex = (IndyException) ex;
			if (ex instanceof ExecutionException && ex.getCause() instanceof IndyException) iex = (IndyException) ex.getCause();
			if (iex instanceof DidAlreadyExistsException) {

				System.err.println("Trust anchor DID has already been created.");
			} else {

				throw new RuntimeException("Cannot create trust anchor DID: " + ex.getMessage(), ex);
			}
		}

		// create ISSUER DID

		CreateAndStoreMyDidJSONParameter createAndStoreMyDidJSONParameter = new CreateAndStoreMyDidJSONParameter(null, seed, null, null);
		CreateAndStoreMyDidResult createAndStoreMyDidResult = null;

		try {

			createAndStoreMyDidResult = Did.createAndStoreMyDid(walletIssuer, createAndStoreMyDidJSONParameter.toJson()).get();
		} catch (IndyException | InterruptedException | ExecutionException ex) {

			IndyException iex = null;
			if (ex instanceof IndyException) iex = (IndyException) ex;
			if (ex instanceof ExecutionException && ex.getCause() instanceof IndyException) iex = (IndyException) ex.getCause();
			if (iex instanceof DidAlreadyExistsException) {

				System.err.println("Issuer DID has already been created.");
			} else {

				throw new RuntimeException("Cannot create issuer DID: " + ex.getMessage(), ex);
			}
		}

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
