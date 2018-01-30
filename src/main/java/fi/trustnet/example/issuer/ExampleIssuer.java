package fi.trustnet.example.issuer;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

import org.abstractj.kalium.NaCl.Sodium;

import com.github.jsonldjava.utils.JsonUtils;

import fi.trustnet.verifiablecredentials.VerifiableCredential;
import info.weboftrust.ldsignatures.LdSignature;
import info.weboftrust.ldsignatures.crypto.EC25519Provider;
import info.weboftrust.ldsignatures.signer.Ed25519Signature2018LdSigner;

public class ExampleIssuer {

	public static void main(String[] args) throws Exception {

		// get issuer DID and private key

		String issuerSeed = "0000000000000000000000000Issuer1";
		byte[] issuerPrivateKey = new byte[Sodium.CRYPTO_SIGN_ED25519_SECRETKEYBYTES];
		byte[] issuerPublicKey = new byte[Sodium.CRYPTO_SIGN_ED25519_PUBLICKEYBYTES];
		EC25519Provider.get().generateEC25519KeyPairFromSeed(issuerPublicKey, issuerPrivateKey, issuerSeed.getBytes(StandardCharsets.UTF_8));
		//		byte[] issuerPrivateKey = Hex.decodeHex("984b589e121040156838303f107e13150be4a80fc5088ccba0b0bdc9b1d89090de8777a28f8da1a74e7a13090ed974d879bf692d001cddee16e4cc9f84b60580".toCharArray());

		String issuerDid = Sovrin.createDid(issuerSeed);

		// get subject DID

		String subjectDid = Sovrin.createDid();

		// issue verifiable credential

		VerifiableCredential verifiableCredential = new VerifiableCredential();
		verifiableCredential.getContext().add("https://trafi.fi/credentials/v1");
		verifiableCredential.getType().add("DriversLicenseCredential");
		verifiableCredential.setIssuer(URI.create(issuerDid));
		verifiableCredential.setIssued("2018-01-01");

		verifiableCredential.setSubject(subjectDid);
		LinkedHashMap<String, Object> jsonLdClaimsObject = verifiableCredential.getJsonLdClaimsObject();
		LinkedHashMap<String, Object> jsonLdDriversLicenseObject = new LinkedHashMap<String, Object> ();
		jsonLdDriversLicenseObject.put("licenseClass", "trucks");
		jsonLdClaimsObject.put("driversLicense", jsonLdDriversLicenseObject);

		URI creator = URI.create(issuerDid + "#key1");
		String created = "2018-01-01T21:19:10Z";
		String domain = null;
		String nonce = "c0ae1c8e-c7e7-469f-b252-86e6a0e7387e";

		Ed25519Signature2018LdSigner signer = new Ed25519Signature2018LdSigner(creator, created, domain, nonce, issuerPrivateKey);
		LdSignature ldSignature = signer.sign(verifiableCredential.getJsonLdObject());

		// output

		System.out.println(ldSignature.getSignatureValue());
		System.out.println(JsonUtils.toPrettyString(verifiableCredential.getJsonLdObject()));
	}
}
