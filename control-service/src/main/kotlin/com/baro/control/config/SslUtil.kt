package com.baro.control.config

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import java.io.FileInputStream
import java.io.FileReader
import java.security.KeyStore
import java.security.Security
import java.security.cert.CertificateFactory
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

object SslUtil {

    init {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    fun createSocketFactory(certPath: String, keyPath: String, caPath: String): SSLSocketFactory {
        val x509 = CertificateFactory.getInstance("X.509")

        val caCert = FileInputStream(caPath).use { x509.generateCertificate(it) }
        val trustStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null)
            setCertificateEntry("ca", caCert)
        }
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
            init(trustStore)
        }

        val clientCert = FileInputStream(certPath).use { x509.generateCertificate(it) }

        // AWS IoT private key is PKCS#1 RSA — BouncyCastle handles both PKCS#1 and PKCS#8
        val pemObject = PEMParser(FileReader(keyPath)).use { it.readObject() }
        val converter = JcaPEMKeyConverter().setProvider("BC")
        val privateKey = when (pemObject) {
            is PEMKeyPair -> converter.getPrivateKey(pemObject.privateKeyInfo)
            is PrivateKeyInfo -> converter.getPrivateKey(pemObject)
            else -> error("Unsupported key format: ${pemObject?.javaClass?.simpleName}")
        }

        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null)
            setKeyEntry("client", privateKey, CharArray(0), arrayOf(clientCert))
        }
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply {
            init(keyStore, CharArray(0))
        }

        return SSLContext.getInstance("TLS").apply {
            init(kmf.keyManagers, tmf.trustManagers, null)
        }.socketFactory
    }
}
