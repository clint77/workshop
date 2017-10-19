= Couchbase Connect Developer Workshop: .NET Core

This workshop uses ASP.NET Core WebAPI functionality to provde the RESTful endpoints for a doctor/patient backend that uses Couchbase.

== How to execute this code

=== Visual Studio 2017

Open the aspnetcore.sln file. Compile and run (you can use Ctrl+F5 to run without debugging, or F5 to run with debugging).

=== dotnet command line

If you are using `dotnet` at the command line:

First, `cd` into the aspnetcore folder that contains aspnetcore.csproj.

Then, execute:

`dotnet restore`

You should only have to do this one time to pull all the necessary packages from NuGet.

Next:

1. `dotnet build`
2. `dotnet run`

`dotnet build` compiles the application, and `dotnet run` executes the application.

After running `dotnet run`, you should see a message: `Now listening on: http://localhost:38043`

=== Visual Studio Code

Again, `cd` into the aspnetcore folder that contains aspnetcore.csproj.

Then, execute:

`code .`

The project will open in Visual Studio Code. You will likely be prompted with messages:

"Required assets to build and debug are missing from 'aspnetcore'. Add them"?

and

"There are unresolved dependencies from 'aspnetcore.csproj'. Please execute the restore command to continue."

Go ahead and follow those prompts.

You can start the web server with F5 or Ctrl+F5 from Visual Studio. There will be a message in the Visual Studio console like:

`Now listening on: http://localhost:5000`