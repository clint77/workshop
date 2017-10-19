<?php
// Application middleware

$app->add(function ($request, $response, $next) {
    // add media parser
    $request->registerMediaTypeParser(
        "application/json",
        function ($input) {
            return json_decode($input, true);
        }
    );

    return $next($request, $response);
});
