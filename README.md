# pension-scheme-return

Pension scheme return is the backend service for pension scheme return which is a feature on manage your pension.

***

## Technical documentation

### Before running the app

Run the following command to start all of the related services for this project:

```bash
sm2 -start PSR_ALL
```

Included in the above command is `PENSION_SCHEME_RETURN`, which is this repository's most recent release.
If you want to run your local version of this code instead, run:

```bash
sm2 -stop PENSION_SCHEME_RETURN
```

then:

```bash
sbt 'run'
```

Note: this service runs on port 10700 by default, but a different port (e.g. 17000) may be specified, as shown in the
example below:

```bash
sbt 'run 17000'
```

***

### Running the test suite

```bash
sbt clean coverage test it:test coverageReport
```

or

You can execute the [runtests.sh](runtests.sh) file to run the tests and generate coverage report easily.

```bash
/bin/bash ./runtests.sh
```

***

### Importing the Postman Collection

In order to use postman script instead of frontend with full journey you need to import the json files these are
collections and environments, into Postman.

1. Open Postman

2. Select collections tab and Import -> File -> Upload-> and select json file from postman folder on your repo

Or

1. Select collections tab and Import > select raw text on pop up > copy and paste the Collection > continue

2. In the Collection tab on the left you should have a collection for PSR

Note: Before you can use the collections **YOU MUST import the EnvironmentVariables**

### Importing Environment Variables

> To use the collections, Postman uses custom environment variables for setting bearer tokens, host names and other
> things. These need importing before you can run any collection.

1. Within Postman, Under My Workspace, select environments tab and Import -> File -> Upload-> and select json file from
   postman folder on your repo

Or

1. Under My Workspace, select environments tab and import > raw text on pop up > copy and paste the
   EnvironmentVariable > continue

2. You should have a list of environments you can now use to run your collections against

3. Set this required EnvironmentVariable as Active by clicking the tick

> #### Environment variable overview
>
> - `pension-scheme-return`         sets the host of the service requests to match the environment
> - `pension-scheme-return-stub`    sets the host of the service requests to match the environment
> - `auth`                          sets the host of the service requests to match the environment
> - `bearer_token`                  sets the Authorization token to satisfy backend authentication

### LOGIN
PSR-Login request in the collection is required in order to hit the secured backend endpoints. 
When you hit the PSR-LOGIN, we obtained the authToken from `auth-login-stub/session` url and setting it to the `bearer_token` field of environment variable. This `bearer_token` then will be used in any subsequent requests for Authorization.  

***

### Useful links

- [confluence](https://confluence.tools.tax.service.gov.uk/display/PSR/Pension+Scheme+Return+Home)

***

### License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").