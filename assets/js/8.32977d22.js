(window.webpackJsonp=window.webpackJsonp||[]).push([[8],{437:function(t,e,o){"use strict";o.r(e);o(160);var a=o(414);const s=o.n(a).a.create({baseURL:"https://api.lilu.org.cn/shushan/huaying",timeout:1e4,method:"get"});s.interceptors.response.use(t=>{const e=t.data;return 2e4!=e.resultCode?(console.log(e.resultMsg),Promise.reject(new Error(e.resultMsg||"Error"))):e.data},t=>(console.log(t),Promise.reject(t)));var n=s;var i={name:"Dog",data:()=>({dogdog:"",date:new Date,btnLoading:!1,isDisable:!1}),created(){this.fetchTodayDog()},methods:{async fetchTodayDog(){n({url:"/today/tgrj"}).then(t=>{this.dogdog=t,this.isDisable=!0})},async fetchRandomDog(){this.btnLoading=!0,n({url:"/random/tgrj"}).then(t=>{this.dogdog=t,this.btnLoading=!1,this.isDisable=!1})}}},r=o(29),d=Object(r.a)(i,(function(){var t=this,e=t._self._c;return e("div",{staticStyle:{"margin-top":"140px"}},[e("h3",[t._v(t._s(t.dogdog))]),t._v(" "),e("p",{staticStyle:{"text-align":"right"}},[t._v(t._s(t._f("formatDate")(t.date)))]),t._v(" "),e("div",{staticStyle:{"text-align":"center"}},[e("el-button",{attrs:{type:"primary",loading:t.btnLoading},on:{click:t.fetchRandomDog}},[t._v("随便舔一舔")]),t._v(" "),e("el-button",{attrs:{type:"primary",disabled:t.isDisable},on:{click:t.fetchTodayDog}},[t._v("今日舔记")])],1),t._v(" "),e("br"),t._v(" "),e("ArticleAds")],1)}),[],!1,null,"6fb377d0",null);e.default=d.exports}}]);