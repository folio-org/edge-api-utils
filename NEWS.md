## 13/03/2023 v1.4.1 Released

### Bugs

* [EDGAPIUTL-22](https://folio-org.atlassian.net/browse/EDGAPIUTL-22) - Remove duplicate maven-source-plugin from pom.xml
* [EDGAPIUTL-24](https://folio-org.atlassian.net/browse/EDGAPIUTL-24) - ClassNotFoundException: com.bettercloud.vault.VaultException

## 08/03/2023 v1.4.0 Released
Quesnelia release with dependency upgrades.

### Technical tasks
* [EDGAPIUTL-20](https://folio-org.atlassian.net/browse/EDGAPIUTL-20) - Quesnelia dependency upgrades: folio-spring-support 8.1.0, aws 1.12.671, vault 6.2.0, …

The vault artifact group and package has changed: In pom.xml and Java code replace `com.bettercloud` with `io.github.jopenlibs`.

## 11/10/2023 v1.3.0 - Released
This release includes changes in token cache for user token

### Technical tasks
* [EDGAPIUTL-15](https://issues.folio.org/browse/EDGAPIUTL-15) - Change in TokenCache for UserToken for RTR

## 22/02/2023 v1.2.0 - Released
This release includes logging improvements configuration.

### Technical tasks
* [EDGAPIUTL-10](https://issues.folio.org/browse/EDGAPIUTL-10) - Logging improvement - Configuration

## 05/07/2022 v1.1.2 - Released
This release includes minor technical changes.

### Stories
* [EDGAPIUTL-7](https://issues.folio.org/browse/EDGAPIUTL-7) - Publish javadoc and sources to maven repository

### Bugs
* [EDGAPIUTL-5](https://issues.folio.org/browse/EDGAPIUTL-5) - Upgrade dependencies

## 05/01/2022 v1.1.1 - Released
Fix log4j2 RCE vulnerability

## 29/09/2021 v1.1.0 - Released
The primary focus of this release was to handle api key parameters

### Stories
* [EDGCMNSPR-6](https://issues.folio.org/browse/EDGCMNSPR-6) - Incorrect api key param name

## 27/05/2021 v1.0.0 - Released
Initial release of `edge-api-utils` library

### Stories
* [EDGAPIUTL-1](https://issues.folio.org/browse/EDGAPIUTL-1) - Initial development of edge-api-utils
