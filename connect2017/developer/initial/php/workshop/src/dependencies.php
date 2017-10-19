<?php
// DIC configuration

$container = $app->getContainer();

// monolog
$container['logger'] = function ($c) {
    $settings = $c->get('settings')['logger'];
    $logger = new Monolog\Logger($settings['name']);
    $logger->pushProcessor(new Monolog\Processor\UidProcessor());
    $logger->pushHandler(new Monolog\Handler\StreamHandler($settings['path'], $settings['level']));
    return $logger;
};

$container['couchbase'] = function($c) {
    $settings = $c->get('settings')['couchbase'];

    $cluster = new Couchbase\Cluster($settings['address']);
    /*
     * Step 1 - Authenticating with a Role Based Account
     * **************** PUT CODE HERE ******************
     */
    $couchbase = $cluster->openBucket($settings['bucket']);

    return $couchbase;
};

// shortcut for bucket name
$container['bucket'] = function($c) {
    return $c->get('settings')['couchbase']['bucket'];
};
