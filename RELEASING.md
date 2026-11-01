# Releasing

`cukes-in-the-belt-parent` and `cukes-in-the-belt-core` are published to Maven
Central through the [Central Portal](https://central.sonatype.com). The
examples and the coverage module are not published.

## One-time setup

1. **Namespace `at.tbloder`**: sign in to <https://central.sonatype.com>,
   *Namespaces* → *Add Namespace* → `at.tbloder`. Verify it with the DNS TXT
   record shown there on the domain `tbloder.at`.
2. **Portal user token**: *Account* → *Generate User Token*.
3. **GPG key** for signing (once, keep the key and the passphrase safe):

   ```bash
   gpg --quick-gen-key "Thomas Bloder <tbloder@me.com>" ed25519 sign 5y
   gpg --keyserver keys.openpgp.org --send-keys <KEY-ID>
   gpg --armor --export-secret-keys <KEY-ID>
   ```

   Central checks signatures against public key servers, so the public key
   has to be uploaded (keys.openpgp.org also needs the e-mail address confirmed).
4. **GitHub environment** `maven-central` (*Settings* → *Environments*), with
   the secrets:

   | Secret | Value |
   |---|---|
   | `MAVEN_CENTRAL_USERNAME` | user name of the Portal user token |
   | `MAVEN_CENTRAL_PASSWORD` | password of the Portal user token |
   | `MAVEN_GPG_KEY` | the armored private key (`-----BEGIN PGP PRIVATE KEY BLOCK-----` ...) |
   | `MAVEN_GPG_PASSPHRASE` | its passphrase |

   Optionally add yourself as *required reviewer* of the environment: every
   release then waits for your approval before anything is uploaded.

## Release

1. Make sure `main` is green.
2. Create a GitHub release with a new tag `v<version>`, e.g. `v1.0.0`, on `main`
   (*Releases* → *Draft a new release*) and publish it.
3. The workflow *Release* sets the version from the tag, builds and tests
   everything, signs and uploads the artifacts. Central publishes them after
   validation; they appear on Maven Central within about 30 minutes.

The version in the POMs stays `x.y.z-SNAPSHOT`, the release version only
exists in the workflow. After a release, raise the snapshot version on `main`
if the next release is not a patch release.

## Check locally

```bash
mvn -Prelease verify -DskipTests -Dgpg.skip
```

builds the sources and javadoc jars (javadoc warnings fail the build).
