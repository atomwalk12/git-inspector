# Scala Style Guide

# Introduction

This style guide outlines the coding conventions for Scala code. It aims to promote consistency, readability, and maintainability, especially within a functional programming paradigm. This style guide is a living document and will be refined and updated as our team's practices and the Scala ecosystem evolve. Feedback and suggestions for improvement are always welcome.

# Key Principles

* **Readability:** Code should be easily understood by all team members, emphasizing clarity over conciseness when necessary.
* **Maintainability:** Code should be modular, well-structured, and easy to modify and extend, minimizing technical debt.
* **Consistency:**  Adhering to a consistent style across all projects improves collaboration, reduces cognitive load, and minimizes errors.
* **Functional Paradigm:** Embrace immutability, pure functions, and higher-order functions to build robust and testable applications.
* **Performance:** While readability and functional principles are paramount, code should be efficient. Consider performance implications without sacrificing clarity.

# Deviations from General Scala Conventions (or Clarifications)

This section clarifies or slightly modifies common Scala conventions to align with our specific needs.

## Line Length
* **Maximum line length:** 100 characters.
    * Modern screens and wider code formats improve readability with slightly longer lines.
    * Long type signatures or chained method calls are often more readable on a single line up to this limit.
    * Break lines logically at operators or commas to maintain readability even when exceeding the limit.

## Indentation
* **Use 2 spaces per indentation level.** (Common Scala convention and recommended for conciseness)

## Imports
* **Group imports:**
    * Standard library imports (`scala.*`, `java.*`)
    * Related third-party library imports
    * Local application/library specific imports (within your project)
* **Absolute imports:** Always use absolute imports for clarity and to avoid ambiguity, especially in larger projects.
* **Import order within groups:** Sort alphabetically.
* **Wildcard imports (`import package._`):** Use sparingly. Prefer explicit imports (`import package.ClassA`, `import package.ClassB`) to enhance code clarity and reduce namespace pollution, especially for larger projects. Wildcard imports are acceptable for:
    * Common utility packages (e.g., `scala.concurrent.ExecutionContext.Implicits.global`) when used in a limited scope.
    * Type aliases or implicit conversions where explicit listing might be verbose.

## Naming Conventions

* **Variables (vals and vars):** Use `camelCase`. Favor `val` (immutable) over `var` (mutable) whenever possible.
    * `userName`, `totalCount`, `processedItems`
* **Constants (within `object`s):** Use `camelCase` for constants defined within `object`s for consistency with Scala conventions. `ALL_CAPS_SNAKE_CASE` is also acceptable if there's a strong team preference, but be consistent within a project. Consider `ALL_CAPS_SNAKE_CASE` primarily for truly immutable, widely shared, and configuration-like constants.
    * `object UserSettings { val maxUsers = 100; val defaultTimeoutMs = 5000 }`
    * `object MathConstants { val PI = 3.14159; val EULER_NUMBER = 2.71828 }`
* **Functions (methods):** Use `camelCase`. Choose descriptive verbs or verb phrases that clearly indicate the function's purpose.
    * `calculateTotal()`, `processData()`, `getUserById()`, `validateInput()`
* **Classes, Traits, Objects:** Use `PascalCase` (CamelCase with the first letter capitalized).
    * `UserManager`, `PaymentProcessor`, `UserRepository`, `DataValidator`
* **Packages:** Use `lowercase`.
    * `com.companyx.users`, `com.companyx.payment`
* **Type Parameters:** Use single uppercase letters, often `A`, `B`, `T`, `U`, `V`. Be descriptive if the context is complex or the type parameter has a specific meaning (e.g., `IdType`).
    * `def processList[T](list: List[T]): List[T]`

## Docstrings (Scaladoc)
* **Use Scaladoc format (`/** ... */` or `///` for single-line).**
* **First sentence:** Concise summary of the object's purpose.
* **For complex functions/classes/traits/objects:** Include detailed descriptions of parameters, return values, effects, type parameters, and exceptions (if applicable, though functional error handling is preferred).
* **Use Scaladoc tags where appropriate:** `@param`, `@return`, `@throws`, `@tparam`, `@example`.

```scala
/**
 * Hashes a password using SHA-256.
 *
 * @param password The password to hash.
 * @return A Try[String] containing the hashed password if successful, or a Failure if an error occurs.
 *         The hashed password is in the format "saltHex:hashedPasswordHex".
 * @example
 * {{{
 * val hashedPasswordTry = hashPassword("mySecretPassword")
 * hashedPasswordTry match {
 *   case Success(hashed) => println(s"Hashed password: $hashed")
 *   case Failure(e) => println(s"Error hashing password: ${e.getMessage}")
 * }
 * }}}
 */
def hashPassword(password: String): Try[String] = {
  // function body here
}
```

## Type Annotations
* **Be explicit with type annotations, especially for public methods and complex types.**  Scala's type inference is powerful, but explicit annotations improve readability and act as documentation.
* **For private methods or simple local variables, type inference can be used to reduce verbosity when the type is obvious from the context.**
* **Use type annotations for function parameters and return types for all publicly accessible functions and methods.**

## Comments
* **Write clear and concise comments explaining the "why" behind the code, not just the "what".**  Focus on the intent and reasoning.
* **Comment sparingly:** Well-written, self-documenting code is preferred. Use comments to clarify complex logic, non-obvious algorithms, or business rules.
* **Use complete sentences and proper grammar in comments.**
* **For complex functions or algorithms, consider adding a block comment at the beginning to explain the overall approach.**

## Functional Programming Best Practices

* **Immutability:**  Favor immutable data structures (`val`, `List`, `Vector`, `Map`, `Set`, etc.) over mutable ones (`var`, `Array`, `mutable.Map`, `mutable.Set`). Minimize mutable state.
* **Pure Functions:**  Strive to write pure functions. Pure functions have no side effects and their output depends only on their inputs. This makes code easier to reason about, test, and compose.
* **Avoid Side Effects:** Minimize side effects in functions. If side effects are necessary (e.g., I/O, logging), isolate them and make them explicit.
* **Higher-Order Functions:** Utilize higher-order functions (`map`, `filter`, `fold`, `flatMap`, etc.) to operate on collections and abstract over control flow.
* **Use `Option` for potential absence of values instead of `null`.**
* **Use `Try`, `Either`, or similar functional error handling constructs instead of relying solely on exceptions for control flow.** Exceptions should primarily be used for truly exceptional, unrecoverable situations.
* **Pattern Matching:**  Leverage pattern matching for destructuring data, handling different cases, and writing concise and expressive code.
* **Composition:** Design functions to be composable. Break down complex logic into smaller, reusable, and testable functions.

## Logging
* **Use a standard logging framework:** Company X uses SLF4J (Simple Logging Facade for Java) with a suitable backend like Logback or Log4j2.
* **Log at appropriate levels:** `DEBUG`, `INFO`, `WARN`, `ERROR`. Use these levels semantically.
* **Provide context in log messages:** Include relevant information (user IDs, request IDs, transaction IDs, etc.) to aid debugging and tracing.
* **Structure log messages for easier parsing and analysis.**

## Error Handling
* **Prefer functional error handling mechanisms like `Try` and `Either` for recoverable errors.**
* **Use `Try` when you are primarily interested in success or failure and want to handle exceptions gracefully.**
* **Use `Either` when you need to differentiate between different types of errors and return specific error information.**
* **Use `try...catch` blocks for truly exceptional and unexpected situations, especially when interacting with external systems or legacy code that may throw exceptions.**
* **Avoid catching broad exceptions like `Exception` unless absolutely necessary. Catch specific exception types to handle them appropriately.**
* **When using `try...catch`, log the exception details at an appropriate level (e.g., `ERROR`) with sufficient context.**

# Tooling
* **Code formatter:**  Scalafmt - Enforces consistent formatting automatically. Configure Scalafmt to align with this style guide.
* **Linter/Static Analysis:**  Scalastyle - Identifies potential issues, style violations, and enforce coding best practices. Configure Scalastyle to align with this style guide and functional programming principles.

# Example

```scala
/** Package for user authentication functionalities. */
package com.companyx.auth

import java.security.MessageDigest
import java.security.SecureRandom
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import org.slf4j.LoggerFactory

object UserAuthenticator {

  private val logger = LoggerFactory.getLogger(getClass)
  private val random = new SecureRandom()

  /**
   * Generates a salt for password hashing.
   *
   * @return A Try[Array[Byte]] containing the salt if successful, or a Failure if an error occurs.
   */
  private def generateSalt(): Try[Array[Byte]] = Try {
    val salt = new Array[Byte](16)
    random.nextBytes(salt)
    salt
  }

  /**
   * Hashes a password using SHA-256 with a randomly generated salt.
   *
   * @param password The password to hash.
   * @return A Try[String] containing the hashed password in the format "saltHex:hashedPasswordHex" if successful,
   *         or a Failure if an error occurs.
   */
  def hashPassword(password: String): Try[String] = {
    generateSalt().flatMap { salt =>
      Try {
        val saltedPassword = salt ++ password.getBytes("UTF-8")
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedPasswordBytes = digest.digest(saltedPassword)
        val saltHex = salt.map("%02x".format(_)).mkString
        val hashedPasswordHex = hashedPasswordBytes.map("%02x".format(_)).mkString
        s"$saltHex:$hashedPasswordHex"
      }
    }
  }

  /**
   * Authenticates a user by verifying the provided password against the stored hashed password.
   *
   * @param username     The username of the user.
   * @param password     The password to authenticate.
   * @param storedHash   The stored hashed password (in "saltHex:hashedPasswordHex" format).
   * @param userRepository An interface for accessing user data.
   * @return A Try[Boolean] representing the authentication result. Success(true) if authenticated, Success(false) if not,
   *         or Failure if an error occurs.
   */
  def authenticateUser(username: String, password: String, storedHash: String, userRepository: UserRepository): Try[Boolean] = {
    Try {
      val parts = storedHash.split(":")
      if (parts.length != 2) {
        logger.warn(s"Authentication failed for user '$username': Invalid stored hash format.")
        false // Or consider returning Failure with a specific exception
      } else {
        val saltHex = parts(0)
        val hashedPasswordHex = parts(1)
        val saltBytes = saltHex.sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)
        val saltedPassword = saltBytes ++ password.getBytes("UTF-8")
        val digest = MessageDigest.getInstance("SHA-256")
        val calculatedHashBytes = digest.digest(saltedPassword)
        val calculatedHashHex = calculatedHashBytes.map("%02x".format(_)).mkString

        if (calculatedHashHex == hashedPasswordHex) {
          logger.info(s"User '$username' authenticated successfully.")
          true
        } else {
          logger.warn(s"Authentication failed for user '$username': Incorrect password.")
          false
        }
      }
    }.recoverWith { // Use recoverWith to handle potential exceptions within the Try block
      case e: Exception =>
        logger.error(s"An error occurred during authentication for user '$username': ${e.getMessage}", e)
        Failure(e) // Re-wrap the exception in a Failure to propagate it if needed, or return Failure(false) if you want to treat errors as authentication failures.
    }
  }
}

trait UserRepository {
  def getUserPasswordHash(username: String): Try[Option[String]]
}
```
