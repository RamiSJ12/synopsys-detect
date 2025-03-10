<#ftl output_format="Markdown">

# Properties

This page lists all [solution_name] properties including deprecated and advanced.  For most use cases, refer to [basic properties](basic-properties.md).

<#list groups as group>

## [${group.groupName}](<#noautoesc>${group.location}.md</#noautoesc>)

| Property | Description |
| --- | --- |
<#list group.simple![] as option>
<#noautoesc>
| [${option.propertyKey}](${option.location}) | <#if option.defaultValue?has_content><p>default: ${option.defaultValue}</p></#if><#if option.hasAcceptableValues> <p>Acceptable Values: ${option.acceptableValues?join(", ")} </p></#if><p><#if option.propertyName?has_content>${option.propertyName}: </#if>${option.description} </p> <#if option.deprecated><p>**DEPRECATED: ${option.deprecatedDescription!"This property is deprecated."} It will be removed in ${option.deprecatedRemoveInVersion}.**</p></#if><#if option.deprecatedValues?has_content><p>**DEPRECATED VALUES:** <#list option.deprecatedValues as deprecatedValue> ${deprecatedValue.value}: ${deprecatedValue.reason}</#list></p></#if> |
</#noautoesc>
</#list>
<#list group.advanced![] as option>
<#noautoesc>
| [${option.propertyKey}](${option.location}) (Advanced)| <#if option.defaultValue?has_content><p>default: ${option.defaultValue}</p></#if><#if option.hasAcceptableValues> <p>Acceptable Values: ${option.acceptableValues?join(", ")} </p></#if><p><#if option.propertyName?has_content>${option.propertyName}: </#if>${option.description} </p> <#if option.deprecated><p>**DEPRECATED: ${option.deprecatedDescription!"This property is deprecated."} It will be removed in ${option.deprecatedRemoveInVersion}.**</p></#if><#if option.deprecatedValues?has_content><p>**DEPRECATED VALUES:** <#list option.deprecatedValues as deprecatedValue> ${deprecatedValue.value}: ${deprecatedValue.reason}</#list></p></#if> |
</#noautoesc>
</#list>
<#list group.deprecated![] as option>
<#noautoesc>
| [${option.propertyKey}](${option.location}) (Deprecated)| <#if option.defaultValue?has_content><p>default: ${option.defaultValue}</p></#if><#if option.hasAcceptableValues> <p>Acceptable Values: ${option.acceptableValues?join(", ")} </p></#if><p><#if option.propertyName?has_content>${option.propertyName}: </#if>${option.description} </p> <#if option.deprecated><p>**DEPRECATED: ${option.deprecatedDescription!"This property is deprecated."} It will be removed in ${option.deprecatedRemoveInVersion}.**</p></#if><#if option.deprecatedValues?has_content><p>**DEPRECATED VALUES:** <#list option.deprecatedValues as deprecatedValue> ${deprecatedValue.value}: ${deprecatedValue.reason}</#list></p></#if> |
</#noautoesc>
</#list>

</#list>

Documentation version: ${program_version}
