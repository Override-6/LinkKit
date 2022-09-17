"use strict";(self.webpackChunkwiki=self.webpackChunkwiki||[]).push([[283],{3905:(e,t,n)=>{n.d(t,{Zo:()=>c,kt:()=>d});var l=n(7294);function r(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function i(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var l=Object.getOwnPropertySymbols(e);t&&(l=l.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,l)}return n}function o(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?i(Object(n),!0).forEach((function(t){r(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):i(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function a(e,t){if(null==e)return{};var n,l,r=function(e,t){if(null==e)return{};var n,l,r={},i=Object.keys(e);for(l=0;l<i.length;l++)n=i[l],t.indexOf(n)>=0||(r[n]=e[n]);return r}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(l=0;l<i.length;l++)n=i[l],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(r[n]=e[n])}return r}var u=l.createContext({}),s=function(e){var t=l.useContext(u),n=t;return e&&(n="function"==typeof e?e(t):o(o({},t),e)),n},c=function(e){var t=s(e.components);return l.createElement(u.Provider,{value:t},e.children)},p={inlineCode:"code",wrapper:function(e){var t=e.children;return l.createElement(l.Fragment,{},t)}},k=l.forwardRef((function(e,t){var n=e.components,r=e.mdxType,i=e.originalType,u=e.parentName,c=a(e,["components","mdxType","originalType","parentName"]),k=s(n),d=r,f=k["".concat(u,".").concat(d)]||k[d]||p[d]||i;return n?l.createElement(f,o(o({ref:t},c),{},{components:n})):l.createElement(f,o({ref:t},c))}));function d(e,t){var n=arguments,r=t&&t.mdxType;if("string"==typeof e||r){var i=n.length,o=new Array(i);o[0]=k;var a={};for(var u in t)hasOwnProperty.call(t,u)&&(a[u]=t[u]);a.originalType=e,a.mdxType="string"==typeof e?e:r,o[1]=a;for(var s=2;s<i;s++)o[s]=n[s];return l.createElement.apply(null,o)}return l.createElement.apply(null,n)}k.displayName="MDXCreateElement"},2226:(e,t,n)=>{n.r(t),n.d(t,{contentTitle:()=>o,default:()=>c,frontMatter:()=>i,metadata:()=>a,toc:()=>u});var l=n(7462),r=(n(7294),n(3905));const i={},o=void 0,a={type:"mdx",permalink:"/Linkit/roadmap",source:"@site/src/pages/roadmap.mdx",description:"Framework",frontMatter:{}},u=[{value:"Framework",id:"framework",level:2},{value:"Website",id:"website",level:2},{value:"Debugger",id:"debugger",level:2}],s={toc:u};function c(e){let{components:t,...n}=e;return(0,r.kt)("wrapper",(0,l.Z)({},s,n,{components:t,mdxType:"MDXLayout"}),(0,r.kt)("h2",{id:"framework"},"Framework"),(0,r.kt)("ul",null,(0,r.kt)("li",null,"\ud83d\udd32 Fully exclude Engine dependency from user-side"),(0,r.kt)("li",null,"\ud83d\udd32 Create a SPI in order to extend some parts of the framework"),(0,r.kt)("li",null,"\u2753 Actions shortener (be able to make complex actions that would require a bunch of objects access to be completed)"),(0,r.kt)("li",null,"\ud83d\udee0\ufe0f Connected Objects",(0,r.kt)("ul",null,(0,r.kt)("li",null,"\ud83d\udd32 Inter-Engine Garbage Collector (GC for unused ConnectedObjects)"),(0,r.kt)("li",null,"\ud83d\udee0 Contracts"),(0,r.kt)("ul",null,(0,r.kt)("li",null,"\u2753 Discuss if we keep modifiers"),(0,r.kt)("li",null,"\ud83d\udee0\ufe0f Better invalid contracts error description"),(0,r.kt)("li",null,"\u2705 Apply contract on classes, methods, fields"),(0,r.kt)("li",null,"\u2705 Ability to specify that an object should become a chipped or mirror object"),(0,r.kt)("li",null,"\u2705 Stub classes for mirroring objects")),(0,r.kt)("li",null,"\ud83d\udee0\ufe0f DSL Language"),(0,r.kt)("ul",null,(0,r.kt)("li",null,"\u2705 Class, methods, fields description support"),(0,r.kt)("li",null,"\u2705 Mirror, chipped objects specification support"),(0,r.kt)("li",null,"\u2705 Stub classes / interfaces"),(0,r.kt)("ul",null,"\u2705 Simple agreement description",(0,r.kt)("li",null,'\ud83d\udd32 Add custom engine tags for agreement description (instead of defaults "owner", "cache_owner" etc)'))))),(0,r.kt)("li",null,"\ud83d\udd32 Connection",(0,r.kt)("ul",null,(0,r.kt)("li",null,"\ud83d\udd32 Fully handle engine disconnection without crash"),(0,r.kt)("li",null,"\ud83d\udd32 Fully handle engine reconnection without crash")))),(0,r.kt)("h2",{id:"website"},"Website"),(0,r.kt)("ul",null,(0,r.kt)("li",null,"\ud83d\udd32 Contribute page"),(0,r.kt)("ul",null,(0,r.kt)("li",null,"\ud83d\udd32 Add rules and a how-to-contribute paragraph"),(0,r.kt)("li",null,"\ud83d\udd32 Add a mini survey for the contributors to help them choose what they could do based on what they like to do"),(0,r.kt)("li",null,"\ud83d\udd32 For each feature that needs contribution, create a page that explains what to do.")),(0,r.kt)("li",null,"\u2705 Funny workers set as a decoration during dev phase")),(0,r.kt)("h2",{id:"debugger"},"Debugger"),(0,r.kt)("ul",null,(0,r.kt)("li",null,"\ud83d\udd32 Traffic Panel"),(0,r.kt)("ul",null,(0,r.kt)("li",null,"\ud83d\udd32 Packet Tab"),(0,r.kt)("ul",null,(0,r.kt)("li",null,"\ud83d\udd32 Ordered list of all packets being sent/received by the engine"),(0,r.kt)("li",null,"\ud83d\udd32 Ability to apply filters on the list to show specific packets"),(0,r.kt)("li",null,"\ud83d\udd32 Insight for each packets on what's happened during serialization/deserialization"),(0,r.kt)("li",null,"\ud83d\udd32 Insight for each packets on what's happened during injection")),(0,r.kt)("li",null,"\ud83d\udd32 Channel Tab"),(0,r.kt)("ul",null,(0,r.kt)("li",null,"\ud83d\udd32 List of opened channels with their nodes information"),(0,r.kt)("li",null,"\ud83d\udd32 List of packet involved into channels (sent and received)"),(0,r.kt)("li",null,"\ud83d\udd32 SIPUs state insights"),(0,r.kt)("ul",null,(0,r.kt)("li",null)))),(0,r.kt)("li",null,"\ud83d\udd32 Workers Panel"),(0,r.kt)("ul",null,(0,r.kt)("li",null,"\ud83d\udd32 Insight of each task of each workers"),(0,r.kt)("ul",null,(0,r.kt)("li",null,"\ud83d\udd32 What subtasks has been created"),(0,r.kt)("li",null,"\ud83d\udd32 If task is paused, know why (is it waiting for a request, for another task etc...)"))),(0,r.kt)("li",null,"\ud83d\udd32 Persistence Panel"),(0,r.kt)("ul",null,(0,r.kt)("li",null,"\ud83d\udd32 Get insights on what happens during the serialization of a packet"),(0,r.kt)("ul",null,(0,r.kt)("li",null,"\ud83d\udd32 Know what presence request are done"),(0,r.kt)("li",null,"\ud83d\udd32 Know what Network Object are sent"),(0,r.kt)("li",null,"\ud83d\udd32 Know what objects are replaced by a reference"),(0,r.kt)("li",null,"\ud83d\udd32 Know what unregistered classes and SyncDef classes are sent")))))}c.isMDXComponent=!0}}]);