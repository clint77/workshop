using System.Collections.Generic;
using System.Linq;
using System.Net;
using Couchbase;
using Couchbase.N1QL;
using Couchbase.Search;
using Microsoft.AspNetCore.Mvc;

namespace aspnetcore.Controllers
{
    public class BaseController : Controller
    {
        // convenience methods to construct nice error messages
        // with the various types of results returned from Couchbase

        protected IActionResult CouchbaseError<T>(IResult<T> result)
        {
            return StatusCode((int)HttpStatusCode.InternalServerError, new
            {
                message = ErrorMessage(result)
            });
        }
        protected IActionResult CouchbaseError(IResult result)
        {
            return StatusCode((int)HttpStatusCode.InternalServerError, new
            {
                message = ErrorMessage(result)
            });
        }

        protected IActionResult CouchbaseError<T>(IQueryResult<T> result)
        {
            return StatusCode((int)HttpStatusCode.InternalServerError, new
            {
                message = ErrorMessage(result)
            });
        }

        protected IActionResult CouchbaseError(ISearchQueryResult result)
        {
            return StatusCode((int)HttpStatusCode.InternalServerError, new
            {
                message = ErrorMessage(result)
            });
        }

        private string ErrorMessage<T>(IQueryResult<T> result)
        {
            var errorMessages = new List<string>();
            if (!string.IsNullOrEmpty(result.Message))
                errorMessages.Add($"Message: '{result.Message}'");
            if (result.Errors != null && result.Errors.Any())
                errorMessages.AddRange(result.Errors.Select(e => $"Error: {e.Message} [{e.Code}]"));
            if (result.Exception != null)
                errorMessages.Add($"Exception: {result.Exception.Message}");
            return string.Join(", ", errorMessages);
        }

        private string ErrorMessage(ISearchQueryResult result)
        {
            var errorMessages = new List<string>();
            if (!string.IsNullOrEmpty(result.Message))
                errorMessages.Add($"Message: '{result.Message}'");
            if (result.Errors != null && result.Errors.Any())
                errorMessages.AddRange(result.Errors.Select(e => $"Error: {e}"));
            if (result.Exception != null)
                errorMessages.Add($"Exception: {result.Exception.Message}");
            return string.Join(", ", errorMessages);
        }

        private string ErrorMessage(IResult result)
        {
            var errorMessages = new List<string>();
            if (!string.IsNullOrEmpty(result.Message))
                errorMessages.Add($"Message: '{result.Message}'");
            if (result.Exception != null)
                errorMessages.Add($"Exception: {result.Exception.Message}");
            return string.Join(", ", errorMessages);
        }
    }
}