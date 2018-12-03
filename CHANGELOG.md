# Changelog

### 1.0.7
- Fixed null pointer exception issue when there are no designs for a page. [#48](https://github.com/DantaFramework/AEM/issues/48)

### 1.0.6
- Exported danta.aem.servlets package.
- Updated acs-aem-commons-bundle version to 3.15.0.
- Fixed issue with KEYWORDS and TAGS page properties. Added new TAGS_LIST property [#43](https://github.com/DantaFramework/AEM/issues/43).
- Exported danta.aem.util package.
- Updated implementation of the template content model to extend AbstractTemplateContentModelImpl in the Core project.
- Updated vanity URL property in page and lists contexts.
- Removed deprecated references, added Value->Object method, updated references.
- Fixed issue for debugging ClientLibs through URL parameter or HTML Library Manager configuration.

### 1.0.5
- Changed project groupId from "danta" to "io.tikaltechnologies.danta".
- Changed project artifactId from "AEM" to "aem".
- Added AddStructureResourcesContextProcessor to include the structure resources of a page to the Content Model.
- Removed properties from default namespaces on page context.
- Updated rules to apply minify option on clientLibrary helper #21.
- Fixed issue with Sling Models to works outside AEM bundle #25.
- This release is available on Maven Central repository.

### 1.0.0
- First release.