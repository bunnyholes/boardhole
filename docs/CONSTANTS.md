# Constant Containers

Certain classes are dedicated to storing application-wide constants. These classes are whitelisted in Checkstyle so that `static final` fields are permitted.

Approved constant container classes:

- `ApiPaths`
- `ValidationConstants`
- `ErrorCode`
- `PermissionType`
- `LogConstants`
- `ColumnNames`
- `JsonKeys`

When introducing a new constants holder, add the class name to `config/checkstyle/suppressions.xml` under the `NoStaticFinal` suppression so the rule remains consistent.
