package fi.trustnet.example.issuer;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

import org.abstractj.kalium.NaCl.Sodium;
import org.apache.commons.codec.binary.Hex;
import org.hyperledger.indy.sdk.did.DidResults.CreateAndStoreMyDidResult;

import com.github.jsonldjava.utils.JsonUtils;

import fi.trustnet.verifiablecredentials.VerifiableCredential;
import info.weboftrust.ldsignatures.LdSignature;
import info.weboftrust.ldsignatures.crypto.EC25519Provider;
import info.weboftrust.ldsignatures.signer.Ed25519Signature2018LdSigner;

public class ExampleIssuer {

	public static void main(String[] args) throws Exception {

		// open Sovrin

		Sovrin.open();

		// create issuer DID

		String issuerSeed = "0000000000000000000000000Issuer1";
		byte[] issuerPrivateKey = new byte[Sodium.CRYPTO_SIGN_ED25519_SECRETKEYBYTES];
		byte[] issuerPublicKey = new byte[Sodium.CRYPTO_SIGN_ED25519_PUBLICKEYBYTES];
		EC25519Provider.get().generateEC25519KeyPairFromSeed(issuerPublicKey, issuerPrivateKey, issuerSeed.getBytes(StandardCharsets.UTF_8));

		CreateAndStoreMyDidResult issuer = Sovrin.createDid(issuerSeed);
		String issuerDid = issuer.getDid();
		String issuerVerkey = issuer.getVerkey();

		System.out.println("Issuer DID: " + issuerDid);
		System.out.println("Issuer DID Verkey: " + issuerVerkey);
		System.out.println("Issuer Private Key: " + Hex.encodeHexString(issuerPrivateKey));
		System.out.println("Issuer Public Key: " + Hex.encodeHexString(issuerPublicKey));

		// get subject DID

		String subjectSeed = "000000000000000000000000Subject1";

		CreateAndStoreMyDidResult subject = Sovrin.createDid(subjectSeed);
		String subjectDid = subject.getDid();
		String subjectVerkey = subject.getVerkey();

		System.out.println("Subject DID: " + subjectDid);
		System.out.println("Subject DID Verkey: " + subjectVerkey);

		// issue Verifiable Credential

		VerifiableCredential verifiableCredential = new VerifiableCredential();
		verifiableCredential.getContext().add("https://trafi.fi/credentials/v1");
		verifiableCredential.getType().add("DriversLicenseCredential");
		verifiableCredential.setIssuer(URI.create("did:sov:" + issuerDid));
		verifiableCredential.setIssued("2018-01-01");

		verifiableCredential.setSubject("did:sov:" + subjectDid);
		LinkedHashMap<String, Object> jsonLdClaimsObject = verifiableCredential.getJsonLdClaimsObject();
		LinkedHashMap<String, Object> jsonLdDriversLicenseObject = new LinkedHashMap<String, Object> ();
		jsonLdDriversLicenseObject.put("licenseClass", "trucks");
		jsonLdClaimsObject.put("driversLicense", jsonLdDriversLicenseObject);

		URI creator = URI.create("did:sov:" + issuerDid + "#key1");
		String created = "2018-01-01T21:19:10Z";
		String domain = null;
		String nonce = "c0ae1c8e-c7e7-469f-b252-86e6a0e7387e";

		// sign

		Ed25519Signature2018LdSigner signer = new Ed25519Signature2018LdSigner(creator, created, domain, nonce, issuerPrivateKey);
		LdSignature ldSignature = signer.sign(verifiableCredential.getJsonLdObject());

		// output

		System.out.println("Signature Value: " + ldSignature.getSignatureValue());
		System.out.println(JsonUtils.toPrettyString(verifiableCredential.getJsonLdObject()));
	}
}
