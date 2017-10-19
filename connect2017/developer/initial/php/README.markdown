This demo is using [Slim](https://www.slimframework.com/) web framework.

To run it navigate to project root:

    $ cd workshop

Install dependencies:

    $ composer install

Run project using PHP built-in webserver:

    $ php -S localhost:8080 -t public public/index.php
    PHP 7.1.10 Development Server started at Fri Oct 13 14:53:14 2017
    Listening on http://localhost:8080
    Document root is /tmp/connect-developer-workshop/initial/php/workshop/public
    Press Ctrl-C to quit.

Or even easier with docker ([Dockerfile](https://hub.docker.com/r/avsej/php-couchbase-base/)):

    $ docker-compose up
