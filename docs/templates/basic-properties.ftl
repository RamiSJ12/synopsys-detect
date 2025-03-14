# Basic Properties

This page lists [solution_name]'s basic properties; for advanced and deprecated properties, refer to [all properties](all-properties.md).

<#list groups as group>

## [${group.groupName}](${group.location}.md)

| Property | Description |
| --- | --- |
<#list group.options as option>
| [${option.propertyKey}](${option.location}) | <#if option.defaultValue?has_content><p>default: ${option.defaultValue}</p></#if><#if option.hasAcceptableValues> <p>Acceptable Values: ${option.acceptableValues?join(", ")} </p></#if><p><#if option.propertyName?has_content>${option.propertyName}: </#if>${option.description} </p> <#if option.deprecated><p>**DEPRECATED: ${option.deprecatedDescription!"This property is deprecated."}**</p></#if><#if option.deprecatedValues?has_content><p>**DEPRECATED VALUES:** <#list option.deprecatedValues as deprecatedValue> ${deprecatedValue.value}: ${deprecatedValue.reason}</#list></p></#if> |
</#list>

</#list>
