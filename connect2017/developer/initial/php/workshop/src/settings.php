<?php
return [
    'settings' => [
        'displayErrorDetails' => true, // set to false in production
        'addContentLengthHeader' => false, // Allow the web server to send the content-length header

        // Monolog settings
        'logger' => [
            'name' => 'slim-app',
            'path' => 'php://stderr',
            'level' => \Monolog\Logger::DEBUG,
        ],

        // Couchbase settings
        'couchbase' => [
            // Use bridge address in docker and localhost otherwise
            'address' => 'couchbase://' . (getenv('docker') ? '172.18.0.1' : '127.0.0.1'),
            'bucket' => 'default',
            'username' => 'demo',
            'password' => '123456'
        ],
    ],
];
