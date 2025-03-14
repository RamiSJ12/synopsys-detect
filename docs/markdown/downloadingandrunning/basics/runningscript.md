# Running the [solution_name] script

The primary function of the [solution_name] scripts is to download and execute the [solution_name] .jar file.
Several aspects of script functionality can be configured, including:

* The [solution_name] version to download/run; by default, the latest version.
* The download location.
* Where to find Java.

Information on how to configure the scripts is in [Shell script configuration](../../scripts/overview.md).

## Running the script on Linux or Mac

On Linux or Mac, execute the [solution_name] script ([bash_script_name], which is a Bash script) from Bash.

To download and run the latest version of [solution_name] in a single command:

````
bash <(curl -s -L https://detect.synopsys.com/detect7.sh)
````

Append any command line arguments to the end, separated by spaces. For example:

````
bash <(curl -s -L https://detect.synopsys.com/detect7.sh) --blackduck.url=https://blackduck.mydomain.com --blackduck.api.token=myaccesstoken
````

See [Quoting and escaping shell script arguments](../../scripts/script-escaping-special-characters.md) for details about quoting and escaping arguments.

### To run a specific version of [solution_name]:

````
export DETECT_LATEST_RELEASE_VERSION={Synopsys Detect version}
bash <(curl -s -L https://detect.synopsys.com/detect7.sh)
````

For example, to run [solution_name] version 5.5.0:

````
export DETECT_LATEST_RELEASE_VERSION=5.5.0
bash <(curl -s -L https://detect.synopsys.com/detect7.sh)
````

#### Windows

On Windows, there are two ways to execute the [solution_name] script ([powershell_script_name], which is a PowerShell script),   
from [Command Prompt](https://en.wikipedia.org/wiki/Cmd.exe) or from inside a PowerShell session. 

To download and run the latest version of [solution_name] in a single command from Command Prompt:

````
powershell "[Net.ServicePointManager]::SecurityProtocol = 'tls12'; irm https://detect.synopsys.com/detect7.ps1?$(Get-Random) | iex; detect"
````

To download and run the latest version of [solution_name] in a single command from PowerShell:
````
[Net.ServicePointManager]::SecurityProtocol = 'tls12'; $Env:DETECT_EXIT_CODE_PASSTHRU=1; irm https://detect.synopsys.com/detect7.ps1?$(Get-Random) | iex; detect
````

_Note that when running the above command, the PowerShell session is not exited. See [here](../../scripts/script-escaping-special-characters.md) for more information on the difference between the two commands._

In both cases, append any command line arguments to the end, separated by spaces. For example:

````
powershell "[Net.ServicePointManager]::SecurityProtocol = 'tls12'; irm https://detect.synopsys.com/detect7.ps1?$(Get-Random) | iex; detect" --blackduck.url=https://blackduck.mydomain.com --blackduck.api.token=myaccesstoken
````

See [Quoting and escaping shell script arguments](../../scripts/script-escaping-special-characters.md) for details about quoting and escaping arguments.

### To run a specific version of [solution_name]:

````
set DETECT_LATEST_RELEASE_VERSION={Synopsys Detect version}
powershell "[Net.ServicePointManager]::SecurityProtocol = 'tls12'; irm https://detect.synopsys.com/detect7.ps1?$(Get-Random) | iex; detect"
````

For example, to run [solution_name] version 5.5.0:

````
set DETECT_LATEST_RELEASE_VERSION=5.5.0
powershell "[Net.ServicePointManager]::SecurityProtocol = 'tls12'; irm https://detect.synopsys.com/detect7.ps1?$(Get-Random) | iex; detect"
````

See [Quoting and escaping shell script arguments](../../scripts/script-escaping-special-characters.md) for details about quoting and escaping arguments.
