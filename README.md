# edge-api-utils

Copyright (C) 2021 The Open Library Foundation

This software is distributed under the terms of the Apache License,
Version 2.0. See the file "[LICENSE](LICENSE)" for more information.

## Introduction
The purpose of this edge API utils is the combine classes/utilities/constants shared by both modules: edge-common and edge-common-spring.

## Additional information


### Secure Stores

Three secure stores currently implemented for safe retrieval of encrypted credentials:

#### EphemeralStore ####

Only intended for _development purposes_.  Credentials are defined in plain text in a specified properties file.  `src/main/resources/ephemeral.properties`

#### AwsParamStore ####

Retrieves credentials from Amazon Web Services Systems Manager (AWS SSM), more specifically the Parameter Store, where they're stored encrypted using a KMS key.  `src.main/resources/aws_ss.properties`

**Key:** `<salt>_<tenantId>_<username>`

e.g. Key=`ab73kbw90e_diku_diku`

#### VaultStore ####

Retrieves credentials from a Vault (https://vaultproject.io).  This was added as a more generic alternative for those not using AWS.  `src/main/resources/vault.properties`

**Key:** `<salt>/<tenantId>`
**Field:** `<username>`

e.g. Key=`ab73kbw90e/diku`, Field=`diku`

## Additional information
There will be a single instance of okapi client per OkapiClientFactory and per tenant, which means that this client should never be closed or else there will be runtime errors. To enforce this behaviour, method close() has been removed from OkapiClient class.     

### Configuration
Please refer to the Configuration sections of [edge-common-spring](https://github.com/folio-org/edge-common-spring/blob/master/README.md) and/or [edge-common](https://github.com/folio-org/edge-common-spring/blob/master/README.md) to see all available system properties and their default values.

### Issue tracker
See project [EDGAPIUTL](https://issues.folio.org/browse/EDGAPIUTL-1)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker).

### Other documentation
Other [modules](https://dev.folio.org/source-code/#server-side) are described,
with further FOLIO Developer documentation at
[dev.folio.org](https://dev.folio.org/)
