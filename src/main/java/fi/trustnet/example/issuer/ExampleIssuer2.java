package fi.trustnet.example.issuer;

import java.net.URI;
import java.util.LinkedHashMap;

import org.hyperledger.indy.sdk.did.DidResults.CreateAndStoreMyDidResult;

import com.github.jsonldjava.utils.JsonUtils;

import fi.trustnet.verifiablecredentials.VerifiableCredential;
import info.weboftrust.ldsignatures.LdSignature;
import info.weboftrust.ldsignatures.signer.LibIndyEd25519Signature2018LdSigner;

public class ExampleIssuer2 {

	public static void main(String[] args) throws Exception {

		// open Sovrin

		Sovrin.open();

		// create issuer DID

		String issuerSeed = "0000000000000000000000000Issuer1";

		CreateAndStoreMyDidResult issuer = Sovrin.createDid(issuerSeed);
		String issuerDid = issuer.getDid();
		String issuerVerkey = issuer.getVerkey();

		System.out.println("Issuer DID: " + issuerDid);
		System.out.println("Issuer DID Verkey: " + issuerVerkey);

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

		LibIndyEd25519Signature2018LdSigner signer = new LibIndyEd25519Signature2018LdSigner(creator, created, domain, nonce, Sovrin.walletIssuer, issuerVerkey);
		LdSignature ldSignature = signer.sign(verifiableCredential.getJsonLdObject());

		// output

		System.out.println("Signature Value: " + ldSignature.getSignatureValue());
		System.out.println(JsonUtils.toPrettyString(verifiableCredential.getJsonLdObject()));
	}
}
