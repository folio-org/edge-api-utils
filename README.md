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

## Configuration

Configuration information is specified in two forms:
1. System Properties - General configuration
1. Properties File - Configuration specific to the desired secure store

### System Properties

Property                 | Default     | Description
------------------------ | ----------- | -------------
`port`                   | `8081`      | Server port to listen on
`okapi_url`              | *required*  | Where to find Okapi (URL)
`secure_store`           | `Ephemeral` | Type of secure store to use.  Valid: `Ephemeral`, `AwsSsm`, `Vault`
`secure_store_props`     | `NA`        | Path to a properties file specifying secure store configuration
`token_cache_ttl_ms`     | `3600000`   | How long to cache JWTs, in milliseconds (ms)
`null_token_cache_ttl_ms`| `30000`     | How long to cache login failure (null JWTs), in milliseconds (ms)
`token_cache_capacity`   | `100`       | Max token cache size
`log_level`              | `INFO`      | Log4j Log Level
`request_timeout_ms`     | `30000`     | Request Timeout
`api_key_sources`        | `PARAM,HEADER,PATH` | Defines the sources (order of precendence) of the API key.

## Additional information
There will be a single instance of okapi client per OkapiClientFactory and per tenant, which means that this client should never be closed or else there will be runtime errors. To enforce this behaviour, method close() has been removed from OkapiClient class.     

### Configuration
Please refer to the [Configuration](https://github.com/folio-org/edge-common-spring/blob/master/README.md) section in the [edge-common](https://github.com/folio-org/edge-common-spring/blob/master/README.md) documentation to see all available system properties and their default values.

### Issue tracker
See project [EDGDEMATIC](https://issues.folio.org/browse/EDGAPIUTL-1)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker).

### Other documentation
Other [modules](https://dev.folio.org/source-code/#server-side) are described,
with further FOLIO Developer documentation at
[dev.folio.org](https://dev.folio.org/)
