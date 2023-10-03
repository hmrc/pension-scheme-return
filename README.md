
# pension-scheme-return

Pension scheme return is the backend service for pension scheme return which is a feature on manage your pension. 

### Running the test suite
```
sbt clean coverage test it:test coverageReport
```

### Importing the Postman Collection

In order to use postman script instead of frontend with full journey you need to import the json files these are collections and environments, into Postman.

1. Open Postman

2. Select collections tab and Import -> File -> Upload-> and select json file from postman folder on your repo

Or

1. Select collections tab and Import > select raw text on pop up > copy and paste the Collection > continue

2. In the Collection tab on the left you should have a collection for PSR

Note: Before you can use the collections **YOU MUST import the EnvironmentVariables**

### Importing Environment Variables

> To use the collections Postman uses custom environment variables for setting bearer tokens, host names and other things. These need importing before you can run any collection.

1. Within Postman,  Under My Workspace, select environments tab and Import -> File -> Upload-> and select json file from postman folder on your repo

Or

1. Under My Workspace, select environments tab and import > raw text on pop up > copy and paste the EnvironmentVariable > continue

2. You should have a list of environments you can now use to run your collections against

3. Set this required EnvironmentVariable as Active by clicking the tick


> #### Environment variable overview
>
> - `pension-scheme-return`   sets the host of the service requests to match the environment


### Useful links
- [confluence](https://confluence.tools.tax.service.gov.uk/display/PSR/Pension+Scheme+Return+Home)

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").