var LXMusicSDK = (() => {
  var __create = Object.create;
  var __defProp = Object.defineProperty;
  var __getOwnPropDesc = Object.getOwnPropertyDescriptor;
  var __getOwnPropNames = Object.getOwnPropertyNames;
  var __getProtoOf = Object.getPrototypeOf;
  var __hasOwnProp = Object.prototype.hasOwnProperty;
  var __esm = (fn, res, err) => function __init() {
    if (err) throw err[0];
    try {
      return fn && (res = (0, fn[__getOwnPropNames(fn)[0]])(fn = 0)), res;
    } catch (e) {
      throw err = [e], e;
    }
  };
  var __commonJS = (cb, mod) => function __require() {
    try {
      return mod || (0, cb[__getOwnPropNames(cb)[0]])((mod = { exports: {} }).exports, mod), mod.exports;
    } catch (e) {
      throw mod = 0, e;
    }
  };
  var __export = (target, all) => {
    for (var name in all)
      __defProp(target, name, { get: all[name], enumerable: true });
  };
  var __copyProps = (to, from, except, desc) => {
    if (from && typeof from === "object" || typeof from === "function") {
      for (let key of __getOwnPropNames(from))
        if (!__hasOwnProp.call(to, key) && key !== except)
          __defProp(to, key, { get: () => from[key], enumerable: !(desc = __getOwnPropDesc(from, key)) || desc.enumerable });
    }
    return to;
  };
  var __toESM = (mod, isNodeMode, target) => (target = mod != null ? __create(__getProtoOf(mod)) : {}, __copyProps(
    // If the importer is in node compatibility mode or this is not an ESM
    // file that has been converted to a CommonJS file using a Babel-
    // compatible transform (i.e. "__esModule" has not been set), then set
    // "default" to the CommonJS "module.exports" for node compatibility.
    isNodeMode || !mod || !mod.__esModule ? __defProp(target, "default", { value: mod, enumerable: true }) : target,
    mod
  ));
  var __toCommonJS = (mod) => __copyProps(__defProp({}, "__esModule", { value: true }), mod);

  // stubs/pako.js
  var pako_exports = {};
  __export(pako_exports, {
    constants: () => constants,
    default: () => pako_default,
    deflate: () => deflate,
    deflateRaw: () => deflateRaw,
    inflate: () => inflate,
    inflateRaw: () => inflateRaw
  });
  var pako_default, inflate, deflate, deflateRaw, inflateRaw, constants;
  var init_pako = __esm({
    "stubs/pako.js"() {
      init_pako();
      pako_default = pako_exports;
      inflate = inflate;
      deflate = deflate;
      deflateRaw = deflateRaw;
      inflateRaw = inflateRaw;
      constants = { Z_NO_FLUSH: 0, Z_FINISH: 4, Z_OK: 0, Z_STREAM_END: 1 };
    }
  });

  // src/utils/musicSdk/kg/vendors/infSign.min.js
  var require_infSign_min = __commonJS({
    "src/utils/musicSdk/kg/vendors/infSign.min.js"(exports, module) {
      !(function(t, n) {
        "object" == typeof exports && "undefined" != typeof module ? module.exports = n() : "function" == typeof define && define.amd ? define(n) : (t = t || self, t.infSign = n());
      })(exports, function() {
        "use strict";
        function t(t2, n2, r2) {
          return n2 in t2 ? Object.defineProperty(t2, n2, { value: r2, enumerable: true, configurable: true, writable: true }) : t2[n2] = r2, t2;
        }
        function n(t2, n2) {
          var r2 = Object.keys(t2);
          if (Object.getOwnPropertySymbols) {
            var e2 = Object.getOwnPropertySymbols(t2);
            n2 && (e2 = e2.filter(function(n3) {
              return Object.getOwnPropertyDescriptor(t2, n3).enumerable;
            })), r2.push.apply(r2, e2);
          }
          return r2;
        }
        function r(r2) {
          for (var e2 = 1; e2 < arguments.length; e2++) {
            var o2 = null != arguments[e2] ? arguments[e2] : {};
            e2 % 2 ? n(o2, true).forEach(function(n2) {
              t(r2, n2, o2[n2]);
            }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(r2, Object.getOwnPropertyDescriptors(o2)) : n(o2).forEach(function(t2) {
              Object.defineProperty(r2, t2, Object.getOwnPropertyDescriptor(o2, t2));
            });
          }
          return r2;
        }
        function e(t2, n2) {
          return n2 = { exports: {} }, t2(n2, n2.exports), n2.exports;
        }
        function o(t2) {
          return !!t2.constructor && "function" == typeof t2.constructor.isBuffer && t2.constructor.isBuffer(t2);
        }
        function i(t2) {
          return "function" == typeof t2.readFloatLE && "function" == typeof t2.slice && o(t2.slice(0, 0));
        }
        function c() {
          var t2, n2 = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : {}, e2 = arguments.length > 1 && void 0 !== arguments[1] ? arguments[1] : "", o2 = arguments.length > 2 && void 0 !== arguments[2] ? arguments[2] : {}, i2 = false, c2 = false, a2 = "json", l2 = r({}, n2), u2 = s.isInClient();
          "function" == typeof o2 ? t2 = o2 : (t2 = o2.callback, i2 = o2.useH5 || false, a2 = o2.postType || "json", c2 = o2.isCDN || false), e2 && ("[object Object]" != Object.prototype.toString.call(e2) ? u2 = false : "urlencoded" == a2 && (u2 = false));
          var f2 = function() {
            var n3 = (/* @__PURE__ */ new Date()).getTime(), i3 = [], s2 = [], u3 = "NVPh5oo715z5DIWAeQlhMDsWXXQV4hwt", f3 = { srcappid: "2919", clientver: "20000", clienttime: n3, mid: n3, uuid: n3, dfid: "-" };
            c2 && (delete f3.clienttime, delete f3.mid, delete f3.uuid, delete f3.dfid), l2 = r({}, f3, {}, l2);
            for (var g2 in l2) i3.push(g2);
            if (i3.sort(), i3.forEach(function(t3) {
              s2.push(t3 + "=" + l2[t3]);
            }), e2) if ("[object Object]" == Object.prototype.toString.call(e2)) if ("json" == a2) s2.push(JSON.stringify(e2));
            else {
              var b = [];
              for (var g2 in e2) b.push(g2 + "=" + e2[g2]);
              s2.push(b.join("&"));
            }
            else s2.push(e2);
            s2.unshift(u3), s2.push(u3), l2.signature = d(s2.join("")), o2.log && (console.log("H5\u7B7E\u540D\u524D\u53C2\u6570", s2), console.log("H5\u7B7E\u540D\u540E\u8FD4\u56DE", l2)), e2 ? t2 && t2(l2, "[object Object]" == Object.prototype.toString.call(e2) && "json" == a2 ? JSON.stringify(e2) : e2) : t2 && t2(l2);
          };
          if (u2 && !i2) {
            var g = false;
            s.mobileCall(764, { get: l2, post: e2 }, function(n3) {
              return !g && (g = true, n3 && n3.status ? (delete n3.status, o2.log && (console.log("\u5BA2\u6237\u7AEF\u7B7E\u540D\u524D\u53C2\u6570", { get: l2, post: e2 }), console.log("\u5BA2\u6237\u7AEF\u7B7E\u540D\u540E\u8FD4\u56DE", r({}, l2, {}, n3))), l2 = r({}, l2, {}, n3), e2 ? t2 && t2(l2, "[object Object]" == Object.prototype.toString.call(e2) && "json" == a2 ? JSON.stringify(e2) : e2) : t2 && t2(l2), false) : (u2 = false, void f2()));
            });
          } else u2 = false, f2();
        }
        "undefined" != typeof globalThis ? globalThis : "undefined" != typeof window ? window : "undefined" != typeof global ? global : "undefined" != typeof self && self;
        var s = e(function(t2, n2) {
          !(function(n3, r2) {
            t2.exports = (function() {
              var t3 = { str2Json: function(t4) {
                var n4 = {};
                if ("[object String]" === Object.prototype.toString.call(t4)) try {
                  n4 = JSON.parse(t4);
                } catch (t5) {
                  n4 = {};
                }
                return n4;
              }, json2Str: function(t4) {
                var n4 = t4;
                if ("string" != typeof t4) try {
                  n4 = JSON.stringify(t4);
                } catch (t5) {
                  n4 = "";
                }
                return n4;
              }, _extend: function(t4, n4) {
                if (n4) for (var r3 in t4) n4.hasOwnProperty(r3) || (n4[r3] = t4[r3]);
                return n4;
              }, formatURL: { browser: "", url: "" }, formatSong: { filename: "", filesize: "", hash: "", bitrate: "", extname: "", duration: "", mvhash: "", m4afilesize: "", "320hash": "", "320filesize": "", sqhash: "", sqfilesize: 0, feetype: 0, isfirst: 0 }, formatMV: { filename: "", singername: "", hash: "", imgurl: "" }, formatShare: { shareName: "", topicName: "", hash: "", listID: "", type: "", suid: "", slid: "", imgUrl: "", filename: "", duration: "", shareData: { linkUrl: "", picUrl: "", content: "", title: "" } }, cbNum: 0, isIOS: false, isKugouAndroid: false, isAndroid: true, loadUrl: function(t4) {
                var n4 = document.createElement("iframe");
                n4.setAttribute("src", t4), n4.setAttribute("style", "display:none;"), n4.setAttribute("height", "0px"), n4.setAttribute("width", "0px"), n4.setAttribute("frameborder", "0"), document.body.appendChild(n4), n4.parentNode.removeChild(n4), n4 = null;
              }, callCmd: function(n4) {
                var r3 = t3;
                if (r3.isKugouAndroid) {
                  var e2 = {}, o2 = "";
                  if (n4.cmd && (e2.cmd = n4.cmd), n4.jsonStr && (e2.jsonStr = n4.jsonStr), n4.callback && (o2 = "kgandroidmobilecall" + ++r3.cbNum + Math.random().toString().substr(2, 9), e2.callback = o2, window[o2] = function(t4, e3) {
                    void 0 !== t4 && ("[object String]" === Object.prototype.toString.call(t4) ? (t4 = "#" === e3 ? decodeURIComponent(t4) : decodeURIComponent(decodeURIComponent(t4)), n4.callback(r3.str2Json(t4))) : n4.callback(t4));
                  }), n4.AndroidCallback) {
                    var i2 = r3.str2Json(n4.jsonStr);
                    i2.AndroidCallback = o2, n4.jsonStr = r3.json2Str(i2), n4.jsonStr && (e2.jsonStr = n4.jsonStr);
                  }
                  var c2 = encodeURIComponent(JSON.stringify(e2)), s2 = "kugoujsbridge://start.kugou_jsbridge/?".concat(c2);
                  r3.loadUrl(s2);
                } else if (r3.isAndroid) {
                  var a2 = "", l2 = "";
                  if (n4.jsonStr) {
                    if (n4.callback && "" !== n4.callback && true === n4.AndroidCallback) {
                      l2 = "kgmobilecall" + ++r3.cbNum + Math.random().toString().substr(2, 9), window[l2] = function(t4, e3) {
                        void 0 !== t4 && ("[object String]" === Object.prototype.toString.call(t4) ? (t4 = "#" === e3 ? decodeURIComponent(t4) : decodeURIComponent(decodeURIComponent(t4)), n4.callback(r3.str2Json(t4))) : n4.callback(t4));
                      };
                      var u2 = r3.str2Json(n4.jsonStr);
                      u2.AndroidCallback = l2, n4.jsonStr = r3.json2Str(u2);
                    }
                    try {
                      a2 = external.superCall(n4.cmd, n4.jsonStr);
                    } catch (t4) {
                    }
                  } else try {
                    a2 = external.superCall(n4.cmd);
                  } catch (t4) {
                  }
                  n4.callback && "" !== n4.callback && "AndroidCallback" != a2 && (a2 = r3.str2Json(a2), n4.callback(a2));
                } else {
                  var f2 = "", d2 = "";
                  n4.callback && (d2 = "kgmobilecall" + ++r3.cbNum + Math.random().toString().substr(2, 9), window[d2] = function(t4) {
                    void 0 !== t4 && n4.callback && ("[object String]" === Object.prototype.toString.call(t4) ? n4.callback(r3.str2Json(t4)) : n4.callback(t4));
                  }), d2 && "" != d2 && n4.jsonStr && (f2 = 'kugouurl://start.music/?{"cmd":' + n4.cmd + ', "jsonStr":' + n4.jsonStr + ', "callback":"' + d2 + '"}'), d2 && "" != d2 && !n4.jsonStr && (f2 = 'kugouurl://start.music/?{"cmd":' + n4.cmd + ', "callback":"' + d2 + '"}'), "" == d2 && n4.jsonStr && (f2 = 'kugouurl://start.music/?{"cmd":' + n4.cmd + ', "jsonStr":' + n4.jsonStr + "}"), "" != d2 || n4.jsonStr || (f2 = 'kugouurl://start.music/?{"cmd":' + n4.cmd + "}"), r3.loadUrl(f2);
                }
              }, formartData: function(n4, r3) {
                n4 && 123 == n4 && r3 && (r3 = t3._extend(t3.formatURL, r3)), n4 && 123 == n4 && r3 && (r3 = t3._extend(t3.formatURL, r3));
              } };
              return { isIOS: t3.isIOS, isKugouAndroid: t3.isKugouAndroid, isAndroid: t3.isAndroid, isInClient: function() {
                return !(!t3.isAndroid && !t3.isKugouAndroid && !t3.isIOS);
              }, mobileCall: function(n4, r3, e2) {
                var o2 = "";
                if (r3 && (o2 = t3.json2Str(r3)), !n4) return console.error("\u8BF7\u8F93\u5165\u547D\u4EE4\u53F7\uFF01"), false;
                var i2 = {};
                n4 && (i2.cmd = n4), "" != o2 && (i2.jsonStr = o2), e2 && (i2.callback = e2), n4 && 186 == n4 && e2 && (i2.AndroidCallback = true), t3.callCmd(i2);
              }, KgWebMobileCall: function(t4, n4) {
                if (t4) try {
                  var r3 = t4.split(".");
                  r3.reduce(function(e2, o2) {
                    if (e2[o2]) {
                      if (o2 === r3[r3.length - 1]) {
                        var i2 = e2[o2];
                        return "function" == typeof i2 ? (e2[o2] = function(t5) {
                          i2 && i2(t5), n4 && n4(t5);
                        }, e2[o2]) : (console.error("\u8BF7\u68C0\u67E5\uFF0C\u5F53\u524D\u73AF\u5883\u53D8\u91CF\u5DF2\u6CE8\u518C\u4E86\u5BF9\u8C61\uFF1A" + t4 + "\uFF0C\u4E14\u8BE5\u5BF9\u8C61\u4E0D\u662F\u65B9\u6CD5"), null);
                      }
                      return e2[o2];
                    }
                    return o2 === r3[r3.length - 1] ? e2[o2] = function(t5) {
                      n4 && n4(t5);
                    } : e2[o2] = new Object(), e2[o2];
                  }, window);
                } catch (t5) {
                }
              } };
            })();
          })();
        }), a = e(function(t2) {
          !(function() {
            var n2 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", r2 = { rotl: function(t3, n3) {
              return t3 << n3 | t3 >>> 32 - n3;
            }, rotr: function(t3, n3) {
              return t3 << 32 - n3 | t3 >>> n3;
            }, endian: function(t3) {
              if (t3.constructor == Number) return 16711935 & r2.rotl(t3, 8) | 4278255360 & r2.rotl(t3, 24);
              for (var n3 = 0; n3 < t3.length; n3++) t3[n3] = r2.endian(t3[n3]);
              return t3;
            }, randomBytes: function(t3) {
              for (var n3 = []; t3 > 0; t3--) n3.push(Math.floor(256 * Math.random()));
              return n3;
            }, bytesToWords: function(t3) {
              for (var n3 = [], r3 = 0, e2 = 0; r3 < t3.length; r3++, e2 += 8) n3[e2 >>> 5] |= t3[r3] << 24 - e2 % 32;
              return n3;
            }, wordsToBytes: function(t3) {
              for (var n3 = [], r3 = 0; r3 < 32 * t3.length; r3 += 8) n3.push(t3[r3 >>> 5] >>> 24 - r3 % 32 & 255);
              return n3;
            }, bytesToHex: function(t3) {
              for (var n3 = [], r3 = 0; r3 < t3.length; r3++) n3.push((t3[r3] >>> 4).toString(16)), n3.push((15 & t3[r3]).toString(16));
              return n3.join("");
            }, hexToBytes: function(t3) {
              for (var n3 = [], r3 = 0; r3 < t3.length; r3 += 2) n3.push(parseInt(t3.substr(r3, 2), 16));
              return n3;
            }, bytesToBase64: function(t3) {
              for (var r3 = [], e2 = 0; e2 < t3.length; e2 += 3) for (var o2 = t3[e2] << 16 | t3[e2 + 1] << 8 | t3[e2 + 2], i2 = 0; i2 < 4; i2++) 8 * e2 + 6 * i2 <= 8 * t3.length ? r3.push(n2.charAt(o2 >>> 6 * (3 - i2) & 63)) : r3.push("=");
              return r3.join("");
            }, base64ToBytes: function(t3) {
              t3 = t3.replace(/[^A-Z0-9+\/]/gi, "");
              for (var r3 = [], e2 = 0, o2 = 0; e2 < t3.length; o2 = ++e2 % 4) 0 != o2 && r3.push((n2.indexOf(t3.charAt(e2 - 1)) & Math.pow(2, -2 * o2 + 8) - 1) << 2 * o2 | n2.indexOf(t3.charAt(e2)) >>> 6 - 2 * o2);
              return r3;
            } };
            t2.exports = r2;
          })();
        }), l = { utf8: { stringToBytes: function(t2) {
          return l.bin.stringToBytes(unescape(encodeURIComponent(t2)));
        }, bytesToString: function(t2) {
          return decodeURIComponent(escape(l.bin.bytesToString(t2)));
        } }, bin: { stringToBytes: function(t2) {
          for (var n2 = [], r2 = 0; r2 < t2.length; r2++) n2.push(255 & t2.charCodeAt(r2));
          return n2;
        }, bytesToString: function(t2) {
          for (var n2 = [], r2 = 0; r2 < t2.length; r2++) n2.push(String.fromCharCode(t2[r2]));
          return n2.join("");
        } } }, u = l, f = function(t2) {
          return null != t2 && (o(t2) || i(t2) || !!t2._isBuffer);
        }, d = e(function(t2) {
          !(function() {
            var n2 = a, r2 = u.utf8, e2 = f, o2 = u.bin, i2 = function(t3, c2) {
              t3.constructor == String ? t3 = c2 && "binary" === c2.encoding ? o2.stringToBytes(t3) : r2.stringToBytes(t3) : e2(t3) ? t3 = Array.prototype.slice.call(t3, 0) : Array.isArray(t3) || (t3 = t3.toString());
              for (var s2 = n2.bytesToWords(t3), a2 = 8 * t3.length, l2 = 1732584193, u2 = -271733879, f2 = -1732584194, d2 = 271733878, g = 0; g < s2.length; g++) s2[g] = 16711935 & (s2[g] << 8 | s2[g] >>> 24) | 4278255360 & (s2[g] << 24 | s2[g] >>> 8);
              s2[a2 >>> 5] |= 128 << a2 % 32, s2[14 + (a2 + 64 >>> 9 << 4)] = a2;
              for (var b = i2._ff, p = i2._gg, h = i2._hh, m = i2._ii, g = 0; g < s2.length; g += 16) {
                var y = l2, j = u2, S = f2, v = d2;
                u2 = m(u2 = m(u2 = m(u2 = m(u2 = h(u2 = h(u2 = h(u2 = h(u2 = p(u2 = p(u2 = p(u2 = p(u2 = b(u2 = b(u2 = b(u2 = b(u2, f2 = b(f2, d2 = b(d2, l2 = b(l2, u2, f2, d2, s2[g + 0], 7, -680876936), u2, f2, s2[g + 1], 12, -389564586), l2, u2, s2[g + 2], 17, 606105819), d2, l2, s2[g + 3], 22, -1044525330), f2 = b(f2, d2 = b(d2, l2 = b(l2, u2, f2, d2, s2[g + 4], 7, -176418897), u2, f2, s2[g + 5], 12, 1200080426), l2, u2, s2[g + 6], 17, -1473231341), d2, l2, s2[g + 7], 22, -45705983), f2 = b(f2, d2 = b(d2, l2 = b(l2, u2, f2, d2, s2[g + 8], 7, 1770035416), u2, f2, s2[g + 9], 12, -1958414417), l2, u2, s2[g + 10], 17, -42063), d2, l2, s2[g + 11], 22, -1990404162), f2 = b(f2, d2 = b(d2, l2 = b(l2, u2, f2, d2, s2[g + 12], 7, 1804603682), u2, f2, s2[g + 13], 12, -40341101), l2, u2, s2[g + 14], 17, -1502002290), d2, l2, s2[g + 15], 22, 1236535329), f2 = p(f2, d2 = p(d2, l2 = p(l2, u2, f2, d2, s2[g + 1], 5, -165796510), u2, f2, s2[g + 6], 9, -1069501632), l2, u2, s2[g + 11], 14, 643717713), d2, l2, s2[g + 0], 20, -373897302), f2 = p(f2, d2 = p(d2, l2 = p(l2, u2, f2, d2, s2[g + 5], 5, -701558691), u2, f2, s2[g + 10], 9, 38016083), l2, u2, s2[g + 15], 14, -660478335), d2, l2, s2[g + 4], 20, -405537848), f2 = p(f2, d2 = p(d2, l2 = p(l2, u2, f2, d2, s2[g + 9], 5, 568446438), u2, f2, s2[g + 14], 9, -1019803690), l2, u2, s2[g + 3], 14, -187363961), d2, l2, s2[g + 8], 20, 1163531501), f2 = p(f2, d2 = p(d2, l2 = p(l2, u2, f2, d2, s2[g + 13], 5, -1444681467), u2, f2, s2[g + 2], 9, -51403784), l2, u2, s2[g + 7], 14, 1735328473), d2, l2, s2[g + 12], 20, -1926607734), f2 = h(f2, d2 = h(d2, l2 = h(l2, u2, f2, d2, s2[g + 5], 4, -378558), u2, f2, s2[g + 8], 11, -2022574463), l2, u2, s2[g + 11], 16, 1839030562), d2, l2, s2[g + 14], 23, -35309556), f2 = h(f2, d2 = h(d2, l2 = h(l2, u2, f2, d2, s2[g + 1], 4, -1530992060), u2, f2, s2[g + 4], 11, 1272893353), l2, u2, s2[g + 7], 16, -155497632), d2, l2, s2[g + 10], 23, -1094730640), f2 = h(f2, d2 = h(d2, l2 = h(l2, u2, f2, d2, s2[g + 13], 4, 681279174), u2, f2, s2[g + 0], 11, -358537222), l2, u2, s2[g + 3], 16, -722521979), d2, l2, s2[g + 6], 23, 76029189), f2 = h(f2, d2 = h(d2, l2 = h(l2, u2, f2, d2, s2[g + 9], 4, -640364487), u2, f2, s2[g + 12], 11, -421815835), l2, u2, s2[g + 15], 16, 530742520), d2, l2, s2[g + 2], 23, -995338651), f2 = m(f2, d2 = m(d2, l2 = m(l2, u2, f2, d2, s2[g + 0], 6, -198630844), u2, f2, s2[g + 7], 10, 1126891415), l2, u2, s2[g + 14], 15, -1416354905), d2, l2, s2[g + 5], 21, -57434055), f2 = m(f2, d2 = m(d2, l2 = m(l2, u2, f2, d2, s2[g + 12], 6, 1700485571), u2, f2, s2[g + 3], 10, -1894986606), l2, u2, s2[g + 10], 15, -1051523), d2, l2, s2[g + 1], 21, -2054922799), f2 = m(f2, d2 = m(d2, l2 = m(l2, u2, f2, d2, s2[g + 8], 6, 1873313359), u2, f2, s2[g + 15], 10, -30611744), l2, u2, s2[g + 6], 15, -1560198380), d2, l2, s2[g + 13], 21, 1309151649), f2 = m(f2, d2 = m(d2, l2 = m(l2, u2, f2, d2, s2[g + 4], 6, -145523070), u2, f2, s2[g + 11], 10, -1120210379), l2, u2, s2[g + 2], 15, 718787259), d2, l2, s2[g + 9], 21, -343485551), l2 = l2 + y >>> 0, u2 = u2 + j >>> 0, f2 = f2 + S >>> 0, d2 = d2 + v >>> 0;
              }
              return n2.endian([l2, u2, f2, d2]);
            };
            i2._ff = function(t3, n3, r3, e3, o3, i3, c2) {
              var s2 = t3 + (n3 & r3 | ~n3 & e3) + (o3 >>> 0) + c2;
              return (s2 << i3 | s2 >>> 32 - i3) + n3;
            }, i2._gg = function(t3, n3, r3, e3, o3, i3, c2) {
              var s2 = t3 + (n3 & e3 | r3 & ~e3) + (o3 >>> 0) + c2;
              return (s2 << i3 | s2 >>> 32 - i3) + n3;
            }, i2._hh = function(t3, n3, r3, e3, o3, i3, c2) {
              var s2 = t3 + (n3 ^ r3 ^ e3) + (o3 >>> 0) + c2;
              return (s2 << i3 | s2 >>> 32 - i3) + n3;
            }, i2._ii = function(t3, n3, r3, e3, o3, i3, c2) {
              var s2 = t3 + (r3 ^ (n3 | ~e3)) + (o3 >>> 0) + c2;
              return (s2 << i3 | s2 >>> 32 - i3) + n3;
            }, i2._blocksize = 16, i2._digestsize = 16, t2.exports = function(t3, r3) {
              if (void 0 === t3 || null === t3) throw new Error("Illegal argument " + t3);
              var e3 = n2.wordsToBytes(i2(t3, r3));
              return r3 && r3.asBytes ? e3 : r3 && r3.asString ? o2.bytesToString(e3) : n2.bytesToHex(e3);
            };
          })();
        });
        return c;
      });
    }
  });

  // node_modules/base64-js/index.js
  var require_base64_js = __commonJS({
    "node_modules/base64-js/index.js"(exports) {
      "use strict";
      exports.byteLength = byteLength;
      exports.toByteArray = toByteArray;
      exports.fromByteArray = fromByteArray;
      var lookup = [];
      var revLookup = [];
      var Arr = typeof Uint8Array !== "undefined" ? Uint8Array : Array;
      var code = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
      for (i = 0, len = code.length; i < len; ++i) {
        lookup[i] = code[i];
        revLookup[code.charCodeAt(i)] = i;
      }
      var i;
      var len;
      revLookup["-".charCodeAt(0)] = 62;
      revLookup["_".charCodeAt(0)] = 63;
      function getLens(b64) {
        var len2 = b64.length;
        if (len2 % 4 > 0) {
          throw new Error("Invalid string. Length must be a multiple of 4");
        }
        var validLen = b64.indexOf("=");
        if (validLen === -1) validLen = len2;
        var placeHoldersLen = validLen === len2 ? 0 : 4 - validLen % 4;
        return [validLen, placeHoldersLen];
      }
      function byteLength(b64) {
        var lens = getLens(b64);
        var validLen = lens[0];
        var placeHoldersLen = lens[1];
        return (validLen + placeHoldersLen) * 3 / 4 - placeHoldersLen;
      }
      function _byteLength(b64, validLen, placeHoldersLen) {
        return (validLen + placeHoldersLen) * 3 / 4 - placeHoldersLen;
      }
      function toByteArray(b64) {
        var tmp;
        var lens = getLens(b64);
        var validLen = lens[0];
        var placeHoldersLen = lens[1];
        var arr = new Arr(_byteLength(b64, validLen, placeHoldersLen));
        var curByte = 0;
        var len2 = placeHoldersLen > 0 ? validLen - 4 : validLen;
        var i2;
        for (i2 = 0; i2 < len2; i2 += 4) {
          tmp = revLookup[b64.charCodeAt(i2)] << 18 | revLookup[b64.charCodeAt(i2 + 1)] << 12 | revLookup[b64.charCodeAt(i2 + 2)] << 6 | revLookup[b64.charCodeAt(i2 + 3)];
          arr[curByte++] = tmp >> 16 & 255;
          arr[curByte++] = tmp >> 8 & 255;
          arr[curByte++] = tmp & 255;
        }
        if (placeHoldersLen === 2) {
          tmp = revLookup[b64.charCodeAt(i2)] << 2 | revLookup[b64.charCodeAt(i2 + 1)] >> 4;
          arr[curByte++] = tmp & 255;
        }
        if (placeHoldersLen === 1) {
          tmp = revLookup[b64.charCodeAt(i2)] << 10 | revLookup[b64.charCodeAt(i2 + 1)] << 4 | revLookup[b64.charCodeAt(i2 + 2)] >> 2;
          arr[curByte++] = tmp >> 8 & 255;
          arr[curByte++] = tmp & 255;
        }
        return arr;
      }
      function tripletToBase64(num) {
        return lookup[num >> 18 & 63] + lookup[num >> 12 & 63] + lookup[num >> 6 & 63] + lookup[num & 63];
      }
      function encodeChunk(uint8, start, end) {
        var tmp;
        var output = [];
        for (var i2 = start; i2 < end; i2 += 3) {
          tmp = (uint8[i2] << 16 & 16711680) + (uint8[i2 + 1] << 8 & 65280) + (uint8[i2 + 2] & 255);
          output.push(tripletToBase64(tmp));
        }
        return output.join("");
      }
      function fromByteArray(uint8) {
        var tmp;
        var len2 = uint8.length;
        var extraBytes = len2 % 3;
        var parts = [];
        var maxChunkLength = 16383;
        for (var i2 = 0, len22 = len2 - extraBytes; i2 < len22; i2 += maxChunkLength) {
          parts.push(encodeChunk(uint8, i2, i2 + maxChunkLength > len22 ? len22 : i2 + maxChunkLength));
        }
        if (extraBytes === 1) {
          tmp = uint8[len2 - 1];
          parts.push(
            lookup[tmp >> 2] + lookup[tmp << 4 & 63] + "=="
          );
        } else if (extraBytes === 2) {
          tmp = (uint8[len2 - 2] << 8) + uint8[len2 - 1];
          parts.push(
            lookup[tmp >> 10] + lookup[tmp >> 4 & 63] + lookup[tmp << 2 & 63] + "="
          );
        }
        return parts.join("");
      }
    }
  });

  // node_modules/ieee754/index.js
  var require_ieee754 = __commonJS({
    "node_modules/ieee754/index.js"(exports) {
      exports.read = function(buffer, offset, isLE, mLen, nBytes) {
        var e, m;
        var eLen = nBytes * 8 - mLen - 1;
        var eMax = (1 << eLen) - 1;
        var eBias = eMax >> 1;
        var nBits = -7;
        var i = isLE ? nBytes - 1 : 0;
        var d = isLE ? -1 : 1;
        var s = buffer[offset + i];
        i += d;
        e = s & (1 << -nBits) - 1;
        s >>= -nBits;
        nBits += eLen;
        for (; nBits > 0; e = e * 256 + buffer[offset + i], i += d, nBits -= 8) {
        }
        m = e & (1 << -nBits) - 1;
        e >>= -nBits;
        nBits += mLen;
        for (; nBits > 0; m = m * 256 + buffer[offset + i], i += d, nBits -= 8) {
        }
        if (e === 0) {
          e = 1 - eBias;
        } else if (e === eMax) {
          return m ? NaN : (s ? -1 : 1) * Infinity;
        } else {
          m = m + Math.pow(2, mLen);
          e = e - eBias;
        }
        return (s ? -1 : 1) * m * Math.pow(2, e - mLen);
      };
      exports.write = function(buffer, value, offset, isLE, mLen, nBytes) {
        var e, m, c;
        var eLen = nBytes * 8 - mLen - 1;
        var eMax = (1 << eLen) - 1;
        var eBias = eMax >> 1;
        var rt = mLen === 23 ? Math.pow(2, -24) - Math.pow(2, -77) : 0;
        var i = isLE ? 0 : nBytes - 1;
        var d = isLE ? 1 : -1;
        var s = value < 0 || value === 0 && 1 / value < 0 ? 1 : 0;
        value = Math.abs(value);
        if (isNaN(value) || value === Infinity) {
          m = isNaN(value) ? 1 : 0;
          e = eMax;
        } else {
          e = Math.floor(Math.log(value) / Math.LN2);
          if (value * (c = Math.pow(2, -e)) < 1) {
            e--;
            c *= 2;
          }
          if (e + eBias >= 1) {
            value += rt / c;
          } else {
            value += rt * Math.pow(2, 1 - eBias);
          }
          if (value * c >= 2) {
            e++;
            c /= 2;
          }
          if (e + eBias >= eMax) {
            m = 0;
            e = eMax;
          } else if (e + eBias >= 1) {
            m = (value * c - 1) * Math.pow(2, mLen);
            e = e + eBias;
          } else {
            m = value * Math.pow(2, eBias - 1) * Math.pow(2, mLen);
            e = 0;
          }
        }
        for (; mLen >= 8; buffer[offset + i] = m & 255, i += d, m /= 256, mLen -= 8) {
        }
        e = e << mLen | m;
        eLen += mLen;
        for (; eLen > 0; buffer[offset + i] = e & 255, i += d, e /= 256, eLen -= 8) {
        }
        buffer[offset + i - d] |= s * 128;
      };
    }
  });

  // node_modules/buffer/index.js
  var require_buffer = __commonJS({
    "node_modules/buffer/index.js"(exports) {
      "use strict";
      var base64 = require_base64_js();
      var ieee754 = require_ieee754();
      var customInspectSymbol = typeof Symbol === "function" && typeof Symbol["for"] === "function" ? Symbol["for"]("nodejs.util.inspect.custom") : null;
      exports.Buffer = Buffer3;
      exports.SlowBuffer = SlowBuffer;
      exports.INSPECT_MAX_BYTES = 50;
      var K_MAX_LENGTH = 2147483647;
      exports.kMaxLength = K_MAX_LENGTH;
      Buffer3.TYPED_ARRAY_SUPPORT = typedArraySupport();
      if (!Buffer3.TYPED_ARRAY_SUPPORT && typeof console !== "undefined" && typeof console.error === "function") {
        console.error(
          "This browser lacks typed array (Uint8Array) support which is required by `buffer` v5.x. Use `buffer` v4.x if you require old browser support."
        );
      }
      function typedArraySupport() {
        try {
          const arr = new Uint8Array(1);
          const proto = { foo: function() {
            return 42;
          } };
          Object.setPrototypeOf(proto, Uint8Array.prototype);
          Object.setPrototypeOf(arr, proto);
          return arr.foo() === 42;
        } catch (e) {
          return false;
        }
      }
      Object.defineProperty(Buffer3.prototype, "parent", {
        enumerable: true,
        get: function() {
          if (!Buffer3.isBuffer(this)) return void 0;
          return this.buffer;
        }
      });
      Object.defineProperty(Buffer3.prototype, "offset", {
        enumerable: true,
        get: function() {
          if (!Buffer3.isBuffer(this)) return void 0;
          return this.byteOffset;
        }
      });
      function createBuffer(length) {
        if (length > K_MAX_LENGTH) {
          throw new RangeError('The value "' + length + '" is invalid for option "size"');
        }
        const buf = new Uint8Array(length);
        Object.setPrototypeOf(buf, Buffer3.prototype);
        return buf;
      }
      function Buffer3(arg, encodingOrOffset, length) {
        if (typeof arg === "number") {
          if (typeof encodingOrOffset === "string") {
            throw new TypeError(
              'The "string" argument must be of type string. Received type number'
            );
          }
          return allocUnsafe(arg);
        }
        return from(arg, encodingOrOffset, length);
      }
      Buffer3.poolSize = 8192;
      function from(value, encodingOrOffset, length) {
        if (typeof value === "string") {
          return fromString(value, encodingOrOffset);
        }
        if (ArrayBuffer.isView(value)) {
          return fromArrayView(value);
        }
        if (value == null) {
          throw new TypeError(
            "The first argument must be one of type string, Buffer, ArrayBuffer, Array, or Array-like Object. Received type " + typeof value
          );
        }
        if (isInstance(value, ArrayBuffer) || value && isInstance(value.buffer, ArrayBuffer)) {
          return fromArrayBuffer(value, encodingOrOffset, length);
        }
        if (typeof SharedArrayBuffer !== "undefined" && (isInstance(value, SharedArrayBuffer) || value && isInstance(value.buffer, SharedArrayBuffer))) {
          return fromArrayBuffer(value, encodingOrOffset, length);
        }
        if (typeof value === "number") {
          throw new TypeError(
            'The "value" argument must not be of type number. Received type number'
          );
        }
        const valueOf = value.valueOf && value.valueOf();
        if (valueOf != null && valueOf !== value) {
          return Buffer3.from(valueOf, encodingOrOffset, length);
        }
        const b = fromObject(value);
        if (b) return b;
        if (typeof Symbol !== "undefined" && Symbol.toPrimitive != null && typeof value[Symbol.toPrimitive] === "function") {
          return Buffer3.from(value[Symbol.toPrimitive]("string"), encodingOrOffset, length);
        }
        throw new TypeError(
          "The first argument must be one of type string, Buffer, ArrayBuffer, Array, or Array-like Object. Received type " + typeof value
        );
      }
      Buffer3.from = function(value, encodingOrOffset, length) {
        return from(value, encodingOrOffset, length);
      };
      Object.setPrototypeOf(Buffer3.prototype, Uint8Array.prototype);
      Object.setPrototypeOf(Buffer3, Uint8Array);
      function assertSize(size) {
        if (typeof size !== "number") {
          throw new TypeError('"size" argument must be of type number');
        } else if (size < 0) {
          throw new RangeError('The value "' + size + '" is invalid for option "size"');
        }
      }
      function alloc(size, fill, encoding) {
        assertSize(size);
        if (size <= 0) {
          return createBuffer(size);
        }
        if (fill !== void 0) {
          return typeof encoding === "string" ? createBuffer(size).fill(fill, encoding) : createBuffer(size).fill(fill);
        }
        return createBuffer(size);
      }
      Buffer3.alloc = function(size, fill, encoding) {
        return alloc(size, fill, encoding);
      };
      function allocUnsafe(size) {
        assertSize(size);
        return createBuffer(size < 0 ? 0 : checked(size) | 0);
      }
      Buffer3.allocUnsafe = function(size) {
        return allocUnsafe(size);
      };
      Buffer3.allocUnsafeSlow = function(size) {
        return allocUnsafe(size);
      };
      function fromString(string, encoding) {
        if (typeof encoding !== "string" || encoding === "") {
          encoding = "utf8";
        }
        if (!Buffer3.isEncoding(encoding)) {
          throw new TypeError("Unknown encoding: " + encoding);
        }
        const length = byteLength(string, encoding) | 0;
        let buf = createBuffer(length);
        const actual = buf.write(string, encoding);
        if (actual !== length) {
          buf = buf.slice(0, actual);
        }
        return buf;
      }
      function fromArrayLike(array) {
        const length = array.length < 0 ? 0 : checked(array.length) | 0;
        const buf = createBuffer(length);
        for (let i = 0; i < length; i += 1) {
          buf[i] = array[i] & 255;
        }
        return buf;
      }
      function fromArrayView(arrayView) {
        if (isInstance(arrayView, Uint8Array)) {
          const copy = new Uint8Array(arrayView);
          return fromArrayBuffer(copy.buffer, copy.byteOffset, copy.byteLength);
        }
        return fromArrayLike(arrayView);
      }
      function fromArrayBuffer(array, byteOffset, length) {
        if (byteOffset < 0 || array.byteLength < byteOffset) {
          throw new RangeError('"offset" is outside of buffer bounds');
        }
        if (array.byteLength < byteOffset + (length || 0)) {
          throw new RangeError('"length" is outside of buffer bounds');
        }
        let buf;
        if (byteOffset === void 0 && length === void 0) {
          buf = new Uint8Array(array);
        } else if (length === void 0) {
          buf = new Uint8Array(array, byteOffset);
        } else {
          buf = new Uint8Array(array, byteOffset, length);
        }
        Object.setPrototypeOf(buf, Buffer3.prototype);
        return buf;
      }
      function fromObject(obj) {
        if (Buffer3.isBuffer(obj)) {
          const len = checked(obj.length) | 0;
          const buf = createBuffer(len);
          if (buf.length === 0) {
            return buf;
          }
          obj.copy(buf, 0, 0, len);
          return buf;
        }
        if (obj.length !== void 0) {
          if (typeof obj.length !== "number" || numberIsNaN(obj.length)) {
            return createBuffer(0);
          }
          return fromArrayLike(obj);
        }
        if (obj.type === "Buffer" && Array.isArray(obj.data)) {
          return fromArrayLike(obj.data);
        }
      }
      function checked(length) {
        if (length >= K_MAX_LENGTH) {
          throw new RangeError("Attempt to allocate Buffer larger than maximum size: 0x" + K_MAX_LENGTH.toString(16) + " bytes");
        }
        return length | 0;
      }
      function SlowBuffer(length) {
        if (+length != length) {
          length = 0;
        }
        return Buffer3.alloc(+length);
      }
      Buffer3.isBuffer = function isBuffer(b) {
        return b != null && b._isBuffer === true && b !== Buffer3.prototype;
      };
      Buffer3.compare = function compare(a, b) {
        if (isInstance(a, Uint8Array)) a = Buffer3.from(a, a.offset, a.byteLength);
        if (isInstance(b, Uint8Array)) b = Buffer3.from(b, b.offset, b.byteLength);
        if (!Buffer3.isBuffer(a) || !Buffer3.isBuffer(b)) {
          throw new TypeError(
            'The "buf1", "buf2" arguments must be one of type Buffer or Uint8Array'
          );
        }
        if (a === b) return 0;
        let x = a.length;
        let y = b.length;
        for (let i = 0, len = Math.min(x, y); i < len; ++i) {
          if (a[i] !== b[i]) {
            x = a[i];
            y = b[i];
            break;
          }
        }
        if (x < y) return -1;
        if (y < x) return 1;
        return 0;
      };
      Buffer3.isEncoding = function isEncoding(encoding) {
        switch (String(encoding).toLowerCase()) {
          case "hex":
          case "utf8":
          case "utf-8":
          case "ascii":
          case "latin1":
          case "binary":
          case "base64":
          case "ucs2":
          case "ucs-2":
          case "utf16le":
          case "utf-16le":
            return true;
          default:
            return false;
        }
      };
      Buffer3.concat = function concat(list, length) {
        if (!Array.isArray(list)) {
          throw new TypeError('"list" argument must be an Array of Buffers');
        }
        if (list.length === 0) {
          return Buffer3.alloc(0);
        }
        let i;
        if (length === void 0) {
          length = 0;
          for (i = 0; i < list.length; ++i) {
            length += list[i].length;
          }
        }
        const buffer = Buffer3.allocUnsafe(length);
        let pos = 0;
        for (i = 0; i < list.length; ++i) {
          let buf = list[i];
          if (isInstance(buf, Uint8Array)) {
            if (pos + buf.length > buffer.length) {
              if (!Buffer3.isBuffer(buf)) buf = Buffer3.from(buf);
              buf.copy(buffer, pos);
            } else {
              Uint8Array.prototype.set.call(
                buffer,
                buf,
                pos
              );
            }
          } else if (!Buffer3.isBuffer(buf)) {
            throw new TypeError('"list" argument must be an Array of Buffers');
          } else {
            buf.copy(buffer, pos);
          }
          pos += buf.length;
        }
        return buffer;
      };
      function byteLength(string, encoding) {
        if (Buffer3.isBuffer(string)) {
          return string.length;
        }
        if (ArrayBuffer.isView(string) || isInstance(string, ArrayBuffer)) {
          return string.byteLength;
        }
        if (typeof string !== "string") {
          throw new TypeError(
            'The "string" argument must be one of type string, Buffer, or ArrayBuffer. Received type ' + typeof string
          );
        }
        const len = string.length;
        const mustMatch = arguments.length > 2 && arguments[2] === true;
        if (!mustMatch && len === 0) return 0;
        let loweredCase = false;
        for (; ; ) {
          switch (encoding) {
            case "ascii":
            case "latin1":
            case "binary":
              return len;
            case "utf8":
            case "utf-8":
              return utf8ToBytes(string).length;
            case "ucs2":
            case "ucs-2":
            case "utf16le":
            case "utf-16le":
              return len * 2;
            case "hex":
              return len >>> 1;
            case "base64":
              return base64ToBytes(string).length;
            default:
              if (loweredCase) {
                return mustMatch ? -1 : utf8ToBytes(string).length;
              }
              encoding = ("" + encoding).toLowerCase();
              loweredCase = true;
          }
        }
      }
      Buffer3.byteLength = byteLength;
      function slowToString(encoding, start, end) {
        let loweredCase = false;
        if (start === void 0 || start < 0) {
          start = 0;
        }
        if (start > this.length) {
          return "";
        }
        if (end === void 0 || end > this.length) {
          end = this.length;
        }
        if (end <= 0) {
          return "";
        }
        end >>>= 0;
        start >>>= 0;
        if (end <= start) {
          return "";
        }
        if (!encoding) encoding = "utf8";
        while (true) {
          switch (encoding) {
            case "hex":
              return hexSlice(this, start, end);
            case "utf8":
            case "utf-8":
              return utf8Slice(this, start, end);
            case "ascii":
              return asciiSlice(this, start, end);
            case "latin1":
            case "binary":
              return latin1Slice(this, start, end);
            case "base64":
              return base64Slice(this, start, end);
            case "ucs2":
            case "ucs-2":
            case "utf16le":
            case "utf-16le":
              return utf16leSlice(this, start, end);
            default:
              if (loweredCase) throw new TypeError("Unknown encoding: " + encoding);
              encoding = (encoding + "").toLowerCase();
              loweredCase = true;
          }
        }
      }
      Buffer3.prototype._isBuffer = true;
      function swap(b, n, m) {
        const i = b[n];
        b[n] = b[m];
        b[m] = i;
      }
      Buffer3.prototype.swap16 = function swap16() {
        const len = this.length;
        if (len % 2 !== 0) {
          throw new RangeError("Buffer size must be a multiple of 16-bits");
        }
        for (let i = 0; i < len; i += 2) {
          swap(this, i, i + 1);
        }
        return this;
      };
      Buffer3.prototype.swap32 = function swap32() {
        const len = this.length;
        if (len % 4 !== 0) {
          throw new RangeError("Buffer size must be a multiple of 32-bits");
        }
        for (let i = 0; i < len; i += 4) {
          swap(this, i, i + 3);
          swap(this, i + 1, i + 2);
        }
        return this;
      };
      Buffer3.prototype.swap64 = function swap64() {
        const len = this.length;
        if (len % 8 !== 0) {
          throw new RangeError("Buffer size must be a multiple of 64-bits");
        }
        for (let i = 0; i < len; i += 8) {
          swap(this, i, i + 7);
          swap(this, i + 1, i + 6);
          swap(this, i + 2, i + 5);
          swap(this, i + 3, i + 4);
        }
        return this;
      };
      Buffer3.prototype.toString = function toString() {
        const length = this.length;
        if (length === 0) return "";
        if (arguments.length === 0) return utf8Slice(this, 0, length);
        return slowToString.apply(this, arguments);
      };
      Buffer3.prototype.toLocaleString = Buffer3.prototype.toString;
      Buffer3.prototype.equals = function equals(b) {
        if (!Buffer3.isBuffer(b)) throw new TypeError("Argument must be a Buffer");
        if (this === b) return true;
        return Buffer3.compare(this, b) === 0;
      };
      Buffer3.prototype.inspect = function inspect() {
        let str = "";
        const max = exports.INSPECT_MAX_BYTES;
        str = this.toString("hex", 0, max).replace(/(.{2})/g, "$1 ").trim();
        if (this.length > max) str += " ... ";
        return "<Buffer " + str + ">";
      };
      if (customInspectSymbol) {
        Buffer3.prototype[customInspectSymbol] = Buffer3.prototype.inspect;
      }
      Buffer3.prototype.compare = function compare(target, start, end, thisStart, thisEnd) {
        if (isInstance(target, Uint8Array)) {
          target = Buffer3.from(target, target.offset, target.byteLength);
        }
        if (!Buffer3.isBuffer(target)) {
          throw new TypeError(
            'The "target" argument must be one of type Buffer or Uint8Array. Received type ' + typeof target
          );
        }
        if (start === void 0) {
          start = 0;
        }
        if (end === void 0) {
          end = target ? target.length : 0;
        }
        if (thisStart === void 0) {
          thisStart = 0;
        }
        if (thisEnd === void 0) {
          thisEnd = this.length;
        }
        if (start < 0 || end > target.length || thisStart < 0 || thisEnd > this.length) {
          throw new RangeError("out of range index");
        }
        if (thisStart >= thisEnd && start >= end) {
          return 0;
        }
        if (thisStart >= thisEnd) {
          return -1;
        }
        if (start >= end) {
          return 1;
        }
        start >>>= 0;
        end >>>= 0;
        thisStart >>>= 0;
        thisEnd >>>= 0;
        if (this === target) return 0;
        let x = thisEnd - thisStart;
        let y = end - start;
        const len = Math.min(x, y);
        const thisCopy = this.slice(thisStart, thisEnd);
        const targetCopy = target.slice(start, end);
        for (let i = 0; i < len; ++i) {
          if (thisCopy[i] !== targetCopy[i]) {
            x = thisCopy[i];
            y = targetCopy[i];
            break;
          }
        }
        if (x < y) return -1;
        if (y < x) return 1;
        return 0;
      };
      function bidirectionalIndexOf(buffer, val, byteOffset, encoding, dir) {
        if (buffer.length === 0) return -1;
        if (typeof byteOffset === "string") {
          encoding = byteOffset;
          byteOffset = 0;
        } else if (byteOffset > 2147483647) {
          byteOffset = 2147483647;
        } else if (byteOffset < -2147483648) {
          byteOffset = -2147483648;
        }
        byteOffset = +byteOffset;
        if (numberIsNaN(byteOffset)) {
          byteOffset = dir ? 0 : buffer.length - 1;
        }
        if (byteOffset < 0) byteOffset = buffer.length + byteOffset;
        if (byteOffset >= buffer.length) {
          if (dir) return -1;
          else byteOffset = buffer.length - 1;
        } else if (byteOffset < 0) {
          if (dir) byteOffset = 0;
          else return -1;
        }
        if (typeof val === "string") {
          val = Buffer3.from(val, encoding);
        }
        if (Buffer3.isBuffer(val)) {
          if (val.length === 0) {
            return -1;
          }
          return arrayIndexOf(buffer, val, byteOffset, encoding, dir);
        } else if (typeof val === "number") {
          val = val & 255;
          if (typeof Uint8Array.prototype.indexOf === "function") {
            if (dir) {
              return Uint8Array.prototype.indexOf.call(buffer, val, byteOffset);
            } else {
              return Uint8Array.prototype.lastIndexOf.call(buffer, val, byteOffset);
            }
          }
          return arrayIndexOf(buffer, [val], byteOffset, encoding, dir);
        }
        throw new TypeError("val must be string, number or Buffer");
      }
      function arrayIndexOf(arr, val, byteOffset, encoding, dir) {
        let indexSize = 1;
        let arrLength = arr.length;
        let valLength = val.length;
        if (encoding !== void 0) {
          encoding = String(encoding).toLowerCase();
          if (encoding === "ucs2" || encoding === "ucs-2" || encoding === "utf16le" || encoding === "utf-16le") {
            if (arr.length < 2 || val.length < 2) {
              return -1;
            }
            indexSize = 2;
            arrLength /= 2;
            valLength /= 2;
            byteOffset /= 2;
          }
        }
        function read(buf, i2) {
          if (indexSize === 1) {
            return buf[i2];
          } else {
            return buf.readUInt16BE(i2 * indexSize);
          }
        }
        let i;
        if (dir) {
          let foundIndex = -1;
          for (i = byteOffset; i < arrLength; i++) {
            if (read(arr, i) === read(val, foundIndex === -1 ? 0 : i - foundIndex)) {
              if (foundIndex === -1) foundIndex = i;
              if (i - foundIndex + 1 === valLength) return foundIndex * indexSize;
            } else {
              if (foundIndex !== -1) i -= i - foundIndex;
              foundIndex = -1;
            }
          }
        } else {
          if (byteOffset + valLength > arrLength) byteOffset = arrLength - valLength;
          for (i = byteOffset; i >= 0; i--) {
            let found = true;
            for (let j = 0; j < valLength; j++) {
              if (read(arr, i + j) !== read(val, j)) {
                found = false;
                break;
              }
            }
            if (found) return i;
          }
        }
        return -1;
      }
      Buffer3.prototype.includes = function includes(val, byteOffset, encoding) {
        return this.indexOf(val, byteOffset, encoding) !== -1;
      };
      Buffer3.prototype.indexOf = function indexOf(val, byteOffset, encoding) {
        return bidirectionalIndexOf(this, val, byteOffset, encoding, true);
      };
      Buffer3.prototype.lastIndexOf = function lastIndexOf(val, byteOffset, encoding) {
        return bidirectionalIndexOf(this, val, byteOffset, encoding, false);
      };
      function hexWrite(buf, string, offset, length) {
        offset = Number(offset) || 0;
        const remaining = buf.length - offset;
        if (!length) {
          length = remaining;
        } else {
          length = Number(length);
          if (length > remaining) {
            length = remaining;
          }
        }
        const strLen = string.length;
        if (length > strLen / 2) {
          length = strLen / 2;
        }
        let i;
        for (i = 0; i < length; ++i) {
          const parsed = parseInt(string.substr(i * 2, 2), 16);
          if (numberIsNaN(parsed)) return i;
          buf[offset + i] = parsed;
        }
        return i;
      }
      function utf8Write(buf, string, offset, length) {
        return blitBuffer(utf8ToBytes(string, buf.length - offset), buf, offset, length);
      }
      function asciiWrite(buf, string, offset, length) {
        return blitBuffer(asciiToBytes(string), buf, offset, length);
      }
      function base64Write(buf, string, offset, length) {
        return blitBuffer(base64ToBytes(string), buf, offset, length);
      }
      function ucs2Write(buf, string, offset, length) {
        return blitBuffer(utf16leToBytes(string, buf.length - offset), buf, offset, length);
      }
      Buffer3.prototype.write = function write(string, offset, length, encoding) {
        if (offset === void 0) {
          encoding = "utf8";
          length = this.length;
          offset = 0;
        } else if (length === void 0 && typeof offset === "string") {
          encoding = offset;
          length = this.length;
          offset = 0;
        } else if (isFinite(offset)) {
          offset = offset >>> 0;
          if (isFinite(length)) {
            length = length >>> 0;
            if (encoding === void 0) encoding = "utf8";
          } else {
            encoding = length;
            length = void 0;
          }
        } else {
          throw new Error(
            "Buffer.write(string, encoding, offset[, length]) is no longer supported"
          );
        }
        const remaining = this.length - offset;
        if (length === void 0 || length > remaining) length = remaining;
        if (string.length > 0 && (length < 0 || offset < 0) || offset > this.length) {
          throw new RangeError("Attempt to write outside buffer bounds");
        }
        if (!encoding) encoding = "utf8";
        let loweredCase = false;
        for (; ; ) {
          switch (encoding) {
            case "hex":
              return hexWrite(this, string, offset, length);
            case "utf8":
            case "utf-8":
              return utf8Write(this, string, offset, length);
            case "ascii":
            case "latin1":
            case "binary":
              return asciiWrite(this, string, offset, length);
            case "base64":
              return base64Write(this, string, offset, length);
            case "ucs2":
            case "ucs-2":
            case "utf16le":
            case "utf-16le":
              return ucs2Write(this, string, offset, length);
            default:
              if (loweredCase) throw new TypeError("Unknown encoding: " + encoding);
              encoding = ("" + encoding).toLowerCase();
              loweredCase = true;
          }
        }
      };
      Buffer3.prototype.toJSON = function toJSON() {
        return {
          type: "Buffer",
          data: Array.prototype.slice.call(this._arr || this, 0)
        };
      };
      function base64Slice(buf, start, end) {
        if (start === 0 && end === buf.length) {
          return base64.fromByteArray(buf);
        } else {
          return base64.fromByteArray(buf.slice(start, end));
        }
      }
      function utf8Slice(buf, start, end) {
        end = Math.min(buf.length, end);
        const res = [];
        let i = start;
        while (i < end) {
          const firstByte = buf[i];
          let codePoint = null;
          let bytesPerSequence = firstByte > 239 ? 4 : firstByte > 223 ? 3 : firstByte > 191 ? 2 : 1;
          if (i + bytesPerSequence <= end) {
            let secondByte, thirdByte, fourthByte, tempCodePoint;
            switch (bytesPerSequence) {
              case 1:
                if (firstByte < 128) {
                  codePoint = firstByte;
                }
                break;
              case 2:
                secondByte = buf[i + 1];
                if ((secondByte & 192) === 128) {
                  tempCodePoint = (firstByte & 31) << 6 | secondByte & 63;
                  if (tempCodePoint > 127) {
                    codePoint = tempCodePoint;
                  }
                }
                break;
              case 3:
                secondByte = buf[i + 1];
                thirdByte = buf[i + 2];
                if ((secondByte & 192) === 128 && (thirdByte & 192) === 128) {
                  tempCodePoint = (firstByte & 15) << 12 | (secondByte & 63) << 6 | thirdByte & 63;
                  if (tempCodePoint > 2047 && (tempCodePoint < 55296 || tempCodePoint > 57343)) {
                    codePoint = tempCodePoint;
                  }
                }
                break;
              case 4:
                secondByte = buf[i + 1];
                thirdByte = buf[i + 2];
                fourthByte = buf[i + 3];
                if ((secondByte & 192) === 128 && (thirdByte & 192) === 128 && (fourthByte & 192) === 128) {
                  tempCodePoint = (firstByte & 15) << 18 | (secondByte & 63) << 12 | (thirdByte & 63) << 6 | fourthByte & 63;
                  if (tempCodePoint > 65535 && tempCodePoint < 1114112) {
                    codePoint = tempCodePoint;
                  }
                }
            }
          }
          if (codePoint === null) {
            codePoint = 65533;
            bytesPerSequence = 1;
          } else if (codePoint > 65535) {
            codePoint -= 65536;
            res.push(codePoint >>> 10 & 1023 | 55296);
            codePoint = 56320 | codePoint & 1023;
          }
          res.push(codePoint);
          i += bytesPerSequence;
        }
        return decodeCodePointsArray(res);
      }
      var MAX_ARGUMENTS_LENGTH = 4096;
      function decodeCodePointsArray(codePoints) {
        const len = codePoints.length;
        if (len <= MAX_ARGUMENTS_LENGTH) {
          return String.fromCharCode.apply(String, codePoints);
        }
        let res = "";
        let i = 0;
        while (i < len) {
          res += String.fromCharCode.apply(
            String,
            codePoints.slice(i, i += MAX_ARGUMENTS_LENGTH)
          );
        }
        return res;
      }
      function asciiSlice(buf, start, end) {
        let ret = "";
        end = Math.min(buf.length, end);
        for (let i = start; i < end; ++i) {
          ret += String.fromCharCode(buf[i] & 127);
        }
        return ret;
      }
      function latin1Slice(buf, start, end) {
        let ret = "";
        end = Math.min(buf.length, end);
        for (let i = start; i < end; ++i) {
          ret += String.fromCharCode(buf[i]);
        }
        return ret;
      }
      function hexSlice(buf, start, end) {
        const len = buf.length;
        if (!start || start < 0) start = 0;
        if (!end || end < 0 || end > len) end = len;
        let out = "";
        for (let i = start; i < end; ++i) {
          out += hexSliceLookupTable[buf[i]];
        }
        return out;
      }
      function utf16leSlice(buf, start, end) {
        const bytes = buf.slice(start, end);
        let res = "";
        for (let i = 0; i < bytes.length - 1; i += 2) {
          res += String.fromCharCode(bytes[i] + bytes[i + 1] * 256);
        }
        return res;
      }
      Buffer3.prototype.slice = function slice(start, end) {
        const len = this.length;
        start = ~~start;
        end = end === void 0 ? len : ~~end;
        if (start < 0) {
          start += len;
          if (start < 0) start = 0;
        } else if (start > len) {
          start = len;
        }
        if (end < 0) {
          end += len;
          if (end < 0) end = 0;
        } else if (end > len) {
          end = len;
        }
        if (end < start) end = start;
        const newBuf = this.subarray(start, end);
        Object.setPrototypeOf(newBuf, Buffer3.prototype);
        return newBuf;
      };
      function checkOffset(offset, ext, length) {
        if (offset % 1 !== 0 || offset < 0) throw new RangeError("offset is not uint");
        if (offset + ext > length) throw new RangeError("Trying to access beyond buffer length");
      }
      Buffer3.prototype.readUintLE = Buffer3.prototype.readUIntLE = function readUIntLE(offset, byteLength2, noAssert) {
        offset = offset >>> 0;
        byteLength2 = byteLength2 >>> 0;
        if (!noAssert) checkOffset(offset, byteLength2, this.length);
        let val = this[offset];
        let mul = 1;
        let i = 0;
        while (++i < byteLength2 && (mul *= 256)) {
          val += this[offset + i] * mul;
        }
        return val;
      };
      Buffer3.prototype.readUintBE = Buffer3.prototype.readUIntBE = function readUIntBE(offset, byteLength2, noAssert) {
        offset = offset >>> 0;
        byteLength2 = byteLength2 >>> 0;
        if (!noAssert) {
          checkOffset(offset, byteLength2, this.length);
        }
        let val = this[offset + --byteLength2];
        let mul = 1;
        while (byteLength2 > 0 && (mul *= 256)) {
          val += this[offset + --byteLength2] * mul;
        }
        return val;
      };
      Buffer3.prototype.readUint8 = Buffer3.prototype.readUInt8 = function readUInt8(offset, noAssert) {
        offset = offset >>> 0;
        if (!noAssert) checkOffset(offset, 1, this.length);
        return this[offset];
      };
      Buffer3.prototype.readUint16LE = Buffer3.prototype.readUInt16LE = function readUInt16LE(offset, noAssert) {
        offset = offset >>> 0;
        if (!noAssert) checkOffset(offset, 2, this.length);
        return this[offset] | this[offset + 1] << 8;
      };
      Buffer3.prototype.readUint16BE = Buffer3.prototype.readUInt16BE = function readUInt16BE(offset, noAssert) {
        offset = offset >>> 0;
        if (!noAssert) checkOffset(offset, 2, this.length);
        return this[offset] << 8 | this[offset + 1];
      };
      Buffer3.prototype.readUint32LE = Buffer3.prototype.readUInt32LE = function readUInt32LE(offset, noAssert) {
        offset = offset >>> 0;
        if (!noAssert) checkOffset(offset, 4, this.length);
        return (this[offset] | this[offset + 1] << 8 | this[offset + 2] << 16) + this[offset + 3] * 16777216;
      };
      Buffer3.prototype.readUint32BE = Buffer3.prototype.readUInt32BE = function readUInt32BE(offset, noAssert) {
        offset = offset >>> 0;
        if (!noAssert) checkOffset(offset, 4, this.length);
        return this[offset] * 16777216 + (this[offset + 1] << 16 | this[offset + 2] << 8 | this[offset + 3]);
      };
      Buffer3.prototype.readBigUInt64LE = defineBigIntMethod(function readBigUInt64LE(offset) {
        offset = offset >>> 0;
        validateNumber(offset, "offset");
        const first = this[offset];
        const last = this[offset + 7];
        if (first === void 0 || last === void 0) {
          boundsError(offset, this.length - 8);
        }
        const lo = first + this[++offset] * 2 ** 8 + this[++offset] * 2 ** 16 + this[++offset] * 2 ** 24;
        const hi = this[++offset] + this[++offset] * 2 ** 8 + this[++offset] * 2 ** 16 + last * 2 ** 24;
        return BigInt(lo) + (BigInt(hi) << BigInt(32));
      });
      Buffer3.prototype.readBigUInt64BE = defineBigIntMethod(function readBigUInt64BE(offset) {
        offset = offset >>> 0;
        validateNumber(offset, "offset");
        const first = this[offset];
        const last = this[offset + 7];
        if (first === void 0 || last === void 0) {
          boundsError(offset, this.length - 8);
        }
        const hi = first * 2 ** 24 + this[++offset] * 2 ** 16 + this[++offset] * 2 ** 8 + this[++offset];
        const lo = this[++offset] * 2 ** 24 + this[++offset] * 2 ** 16 + this[++offset] * 2 ** 8 + last;
        return (BigInt(hi) << BigInt(32)) + BigInt(lo);
      });
      Buffer3.prototype.readIntLE = function readIntLE(offset, byteLength2, noAssert) {
        offset = offset >>> 0;
        byteLength2 = byteLength2 >>> 0;
        if (!noAssert) checkOffset(offset, byteLength2, this.length);
        let val = this[offset];
        let mul = 1;
        let i = 0;
        while (++i < byteLength2 && (mul *= 256)) {
          val += this[offset + i] * mul;
        }
        mul *= 128;
        if (val >= mul) val -= Math.pow(2, 8 * byteLength2);
        return val;
      };
      Buffer3.prototype.readIntBE = function readIntBE(offset, byteLength2, noAssert) {
        offset = offset >>> 0;
        byteLength2 = byteLength2 >>> 0;
        if (!noAssert) checkOffset(offset, byteLength2, this.length);
        let i = byteLength2;
        let mul = 1;
        let val = this[offset + --i];
        while (i > 0 && (mul *= 256)) {
          val += this[offset + --i] * mul;
        }
        mul *= 128;
        if (val >= mul) val -= Math.pow(2, 8 * byteLength2);
        return val;
      };
      Buffer3.prototype.readInt8 = function readInt8(offset, noAssert) {
        offset = offset >>> 0;
        if (!noAssert) checkOffset(offset, 1, this.length);
        if (!(this[offset] & 128)) return this[offset];
        return (255 - this[offset] + 1) * -1;
      };
      Buffer3.prototype.readInt16LE = function readInt16LE(offset, noAssert) {
        offset = offset >>> 0;
        if (!noAssert) checkOffset(offset, 2, this.length);
        const val = this[offset] | this[offset + 1] << 8;
        return val & 32768 ? val | 4294901760 : val;
      };
      Buffer3.prototype.readInt16BE = function readInt16BE(offset, noAssert) {
        offset = offset >>> 0;
        if (!noAssert) checkOffset(offset, 2, this.length);
        const val = this[offset + 1] | this[offset] << 8;
        return val & 32768 ? val | 4294901760 : val;
      };
      Buffer3.prototype.readInt32LE = function readInt32LE(offset, noAssert) {
        offset = offset >>> 0;
        if (!noAssert) checkOffset(offset, 4, this.length);
        return this[offset] | this[offset + 1] << 8 | this[offset + 2] << 16 | this[offset + 3] << 24;
      };
      Buffer3.prototype.readInt32BE = function readInt32BE(offset, noAssert) {
        offset = offset >>> 0;
        if (!noAssert) checkOffset(offset, 4, this.length);
        return this[offset] << 24 | this[offset + 1] << 16 | this[offset + 2] << 8 | this[offset + 3];
      };
      Buffer3.prototype.readBigInt64LE = defineBigIntMethod(function readBigInt64LE(offset) {
        offset = offset >>> 0;
        validateNumber(offset, "offset");
        const first = this[offset];
        const last = this[offset + 7];
        if (first === void 0 || last === void 0) {
          boundsError(offset, this.length - 8);
        }
        const val = this[offset + 4] + this[offset + 5] * 2 ** 8 + this[offset + 6] * 2 ** 16 + (last << 24);
        return (BigInt(val) << BigInt(32)) + BigInt(first + this[++offset] * 2 ** 8 + this[++offset] * 2 ** 16 + this[++offset] * 2 ** 24);
      });
      Buffer3.prototype.readBigInt64BE = defineBigIntMethod(function readBigInt64BE(offset) {
        offset = offset >>> 0;
        validateNumber(offset, "offset");
        const first = this[offset];
        const last = this[offset + 7];
        if (first === void 0 || last === void 0) {
          boundsError(offset, this.length - 8);
        }
        const val = (first << 24) + // Overflow
        this[++offset] * 2 ** 16 + this[++offset] * 2 ** 8 + this[++offset];
        return (BigInt(val) << BigInt(32)) + BigInt(this[++offset] * 2 ** 24 + this[++offset] * 2 ** 16 + this[++offset] * 2 ** 8 + last);
      });
      Buffer3.prototype.readFloatLE = function readFloatLE(offset, noAssert) {
        offset = offset >>> 0;
        if (!noAssert) checkOffset(offset, 4, this.length);
        return ieee754.read(this, offset, true, 23, 4);
      };
      Buffer3.prototype.readFloatBE = function readFloatBE(offset, noAssert) {
        offset = offset >>> 0;
        if (!noAssert) checkOffset(offset, 4, this.length);
        return ieee754.read(this, offset, false, 23, 4);
      };
      Buffer3.prototype.readDoubleLE = function readDoubleLE(offset, noAssert) {
        offset = offset >>> 0;
        if (!noAssert) checkOffset(offset, 8, this.length);
        return ieee754.read(this, offset, true, 52, 8);
      };
      Buffer3.prototype.readDoubleBE = function readDoubleBE(offset, noAssert) {
        offset = offset >>> 0;
        if (!noAssert) checkOffset(offset, 8, this.length);
        return ieee754.read(this, offset, false, 52, 8);
      };
      function checkInt(buf, value, offset, ext, max, min) {
        if (!Buffer3.isBuffer(buf)) throw new TypeError('"buffer" argument must be a Buffer instance');
        if (value > max || value < min) throw new RangeError('"value" argument is out of bounds');
        if (offset + ext > buf.length) throw new RangeError("Index out of range");
      }
      Buffer3.prototype.writeUintLE = Buffer3.prototype.writeUIntLE = function writeUIntLE(value, offset, byteLength2, noAssert) {
        value = +value;
        offset = offset >>> 0;
        byteLength2 = byteLength2 >>> 0;
        if (!noAssert) {
          const maxBytes = Math.pow(2, 8 * byteLength2) - 1;
          checkInt(this, value, offset, byteLength2, maxBytes, 0);
        }
        let mul = 1;
        let i = 0;
        this[offset] = value & 255;
        while (++i < byteLength2 && (mul *= 256)) {
          this[offset + i] = value / mul & 255;
        }
        return offset + byteLength2;
      };
      Buffer3.prototype.writeUintBE = Buffer3.prototype.writeUIntBE = function writeUIntBE(value, offset, byteLength2, noAssert) {
        value = +value;
        offset = offset >>> 0;
        byteLength2 = byteLength2 >>> 0;
        if (!noAssert) {
          const maxBytes = Math.pow(2, 8 * byteLength2) - 1;
          checkInt(this, value, offset, byteLength2, maxBytes, 0);
        }
        let i = byteLength2 - 1;
        let mul = 1;
        this[offset + i] = value & 255;
        while (--i >= 0 && (mul *= 256)) {
          this[offset + i] = value / mul & 255;
        }
        return offset + byteLength2;
      };
      Buffer3.prototype.writeUint8 = Buffer3.prototype.writeUInt8 = function writeUInt8(value, offset, noAssert) {
        value = +value;
        offset = offset >>> 0;
        if (!noAssert) checkInt(this, value, offset, 1, 255, 0);
        this[offset] = value & 255;
        return offset + 1;
      };
      Buffer3.prototype.writeUint16LE = Buffer3.prototype.writeUInt16LE = function writeUInt16LE(value, offset, noAssert) {
        value = +value;
        offset = offset >>> 0;
        if (!noAssert) checkInt(this, value, offset, 2, 65535, 0);
        this[offset] = value & 255;
        this[offset + 1] = value >>> 8;
        return offset + 2;
      };
      Buffer3.prototype.writeUint16BE = Buffer3.prototype.writeUInt16BE = function writeUInt16BE(value, offset, noAssert) {
        value = +value;
        offset = offset >>> 0;
        if (!noAssert) checkInt(this, value, offset, 2, 65535, 0);
        this[offset] = value >>> 8;
        this[offset + 1] = value & 255;
        return offset + 2;
      };
      Buffer3.prototype.writeUint32LE = Buffer3.prototype.writeUInt32LE = function writeUInt32LE(value, offset, noAssert) {
        value = +value;
        offset = offset >>> 0;
        if (!noAssert) checkInt(this, value, offset, 4, 4294967295, 0);
        this[offset + 3] = value >>> 24;
        this[offset + 2] = value >>> 16;
        this[offset + 1] = value >>> 8;
        this[offset] = value & 255;
        return offset + 4;
      };
      Buffer3.prototype.writeUint32BE = Buffer3.prototype.writeUInt32BE = function writeUInt32BE(value, offset, noAssert) {
        value = +value;
        offset = offset >>> 0;
        if (!noAssert) checkInt(this, value, offset, 4, 4294967295, 0);
        this[offset] = value >>> 24;
        this[offset + 1] = value >>> 16;
        this[offset + 2] = value >>> 8;
        this[offset + 3] = value & 255;
        return offset + 4;
      };
      function wrtBigUInt64LE(buf, value, offset, min, max) {
        checkIntBI(value, min, max, buf, offset, 7);
        let lo = Number(value & BigInt(4294967295));
        buf[offset++] = lo;
        lo = lo >> 8;
        buf[offset++] = lo;
        lo = lo >> 8;
        buf[offset++] = lo;
        lo = lo >> 8;
        buf[offset++] = lo;
        let hi = Number(value >> BigInt(32) & BigInt(4294967295));
        buf[offset++] = hi;
        hi = hi >> 8;
        buf[offset++] = hi;
        hi = hi >> 8;
        buf[offset++] = hi;
        hi = hi >> 8;
        buf[offset++] = hi;
        return offset;
      }
      function wrtBigUInt64BE(buf, value, offset, min, max) {
        checkIntBI(value, min, max, buf, offset, 7);
        let lo = Number(value & BigInt(4294967295));
        buf[offset + 7] = lo;
        lo = lo >> 8;
        buf[offset + 6] = lo;
        lo = lo >> 8;
        buf[offset + 5] = lo;
        lo = lo >> 8;
        buf[offset + 4] = lo;
        let hi = Number(value >> BigInt(32) & BigInt(4294967295));
        buf[offset + 3] = hi;
        hi = hi >> 8;
        buf[offset + 2] = hi;
        hi = hi >> 8;
        buf[offset + 1] = hi;
        hi = hi >> 8;
        buf[offset] = hi;
        return offset + 8;
      }
      Buffer3.prototype.writeBigUInt64LE = defineBigIntMethod(function writeBigUInt64LE(value, offset = 0) {
        return wrtBigUInt64LE(this, value, offset, BigInt(0), BigInt("0xffffffffffffffff"));
      });
      Buffer3.prototype.writeBigUInt64BE = defineBigIntMethod(function writeBigUInt64BE(value, offset = 0) {
        return wrtBigUInt64BE(this, value, offset, BigInt(0), BigInt("0xffffffffffffffff"));
      });
      Buffer3.prototype.writeIntLE = function writeIntLE(value, offset, byteLength2, noAssert) {
        value = +value;
        offset = offset >>> 0;
        if (!noAssert) {
          const limit = Math.pow(2, 8 * byteLength2 - 1);
          checkInt(this, value, offset, byteLength2, limit - 1, -limit);
        }
        let i = 0;
        let mul = 1;
        let sub = 0;
        this[offset] = value & 255;
        while (++i < byteLength2 && (mul *= 256)) {
          if (value < 0 && sub === 0 && this[offset + i - 1] !== 0) {
            sub = 1;
          }
          this[offset + i] = (value / mul >> 0) - sub & 255;
        }
        return offset + byteLength2;
      };
      Buffer3.prototype.writeIntBE = function writeIntBE(value, offset, byteLength2, noAssert) {
        value = +value;
        offset = offset >>> 0;
        if (!noAssert) {
          const limit = Math.pow(2, 8 * byteLength2 - 1);
          checkInt(this, value, offset, byteLength2, limit - 1, -limit);
        }
        let i = byteLength2 - 1;
        let mul = 1;
        let sub = 0;
        this[offset + i] = value & 255;
        while (--i >= 0 && (mul *= 256)) {
          if (value < 0 && sub === 0 && this[offset + i + 1] !== 0) {
            sub = 1;
          }
          this[offset + i] = (value / mul >> 0) - sub & 255;
        }
        return offset + byteLength2;
      };
      Buffer3.prototype.writeInt8 = function writeInt8(value, offset, noAssert) {
        value = +value;
        offset = offset >>> 0;
        if (!noAssert) checkInt(this, value, offset, 1, 127, -128);
        if (value < 0) value = 255 + value + 1;
        this[offset] = value & 255;
        return offset + 1;
      };
      Buffer3.prototype.writeInt16LE = function writeInt16LE(value, offset, noAssert) {
        value = +value;
        offset = offset >>> 0;
        if (!noAssert) checkInt(this, value, offset, 2, 32767, -32768);
        this[offset] = value & 255;
        this[offset + 1] = value >>> 8;
        return offset + 2;
      };
      Buffer3.prototype.writeInt16BE = function writeInt16BE(value, offset, noAssert) {
        value = +value;
        offset = offset >>> 0;
        if (!noAssert) checkInt(this, value, offset, 2, 32767, -32768);
        this[offset] = value >>> 8;
        this[offset + 1] = value & 255;
        return offset + 2;
      };
      Buffer3.prototype.writeInt32LE = function writeInt32LE(value, offset, noAssert) {
        value = +value;
        offset = offset >>> 0;
        if (!noAssert) checkInt(this, value, offset, 4, 2147483647, -2147483648);
        this[offset] = value & 255;
        this[offset + 1] = value >>> 8;
        this[offset + 2] = value >>> 16;
        this[offset + 3] = value >>> 24;
        return offset + 4;
      };
      Buffer3.prototype.writeInt32BE = function writeInt32BE(value, offset, noAssert) {
        value = +value;
        offset = offset >>> 0;
        if (!noAssert) checkInt(this, value, offset, 4, 2147483647, -2147483648);
        if (value < 0) value = 4294967295 + value + 1;
        this[offset] = value >>> 24;
        this[offset + 1] = value >>> 16;
        this[offset + 2] = value >>> 8;
        this[offset + 3] = value & 255;
        return offset + 4;
      };
      Buffer3.prototype.writeBigInt64LE = defineBigIntMethod(function writeBigInt64LE(value, offset = 0) {
        return wrtBigUInt64LE(this, value, offset, -BigInt("0x8000000000000000"), BigInt("0x7fffffffffffffff"));
      });
      Buffer3.prototype.writeBigInt64BE = defineBigIntMethod(function writeBigInt64BE(value, offset = 0) {
        return wrtBigUInt64BE(this, value, offset, -BigInt("0x8000000000000000"), BigInt("0x7fffffffffffffff"));
      });
      function checkIEEE754(buf, value, offset, ext, max, min) {
        if (offset + ext > buf.length) throw new RangeError("Index out of range");
        if (offset < 0) throw new RangeError("Index out of range");
      }
      function writeFloat(buf, value, offset, littleEndian, noAssert) {
        value = +value;
        offset = offset >>> 0;
        if (!noAssert) {
          checkIEEE754(buf, value, offset, 4, 34028234663852886e22, -34028234663852886e22);
        }
        ieee754.write(buf, value, offset, littleEndian, 23, 4);
        return offset + 4;
      }
      Buffer3.prototype.writeFloatLE = function writeFloatLE(value, offset, noAssert) {
        return writeFloat(this, value, offset, true, noAssert);
      };
      Buffer3.prototype.writeFloatBE = function writeFloatBE(value, offset, noAssert) {
        return writeFloat(this, value, offset, false, noAssert);
      };
      function writeDouble(buf, value, offset, littleEndian, noAssert) {
        value = +value;
        offset = offset >>> 0;
        if (!noAssert) {
          checkIEEE754(buf, value, offset, 8, 17976931348623157e292, -17976931348623157e292);
        }
        ieee754.write(buf, value, offset, littleEndian, 52, 8);
        return offset + 8;
      }
      Buffer3.prototype.writeDoubleLE = function writeDoubleLE(value, offset, noAssert) {
        return writeDouble(this, value, offset, true, noAssert);
      };
      Buffer3.prototype.writeDoubleBE = function writeDoubleBE(value, offset, noAssert) {
        return writeDouble(this, value, offset, false, noAssert);
      };
      Buffer3.prototype.copy = function copy(target, targetStart, start, end) {
        if (!Buffer3.isBuffer(target)) throw new TypeError("argument should be a Buffer");
        if (!start) start = 0;
        if (!end && end !== 0) end = this.length;
        if (targetStart >= target.length) targetStart = target.length;
        if (!targetStart) targetStart = 0;
        if (end > 0 && end < start) end = start;
        if (end === start) return 0;
        if (target.length === 0 || this.length === 0) return 0;
        if (targetStart < 0) {
          throw new RangeError("targetStart out of bounds");
        }
        if (start < 0 || start >= this.length) throw new RangeError("Index out of range");
        if (end < 0) throw new RangeError("sourceEnd out of bounds");
        if (end > this.length) end = this.length;
        if (target.length - targetStart < end - start) {
          end = target.length - targetStart + start;
        }
        const len = end - start;
        if (this === target && typeof Uint8Array.prototype.copyWithin === "function") {
          this.copyWithin(targetStart, start, end);
        } else {
          Uint8Array.prototype.set.call(
            target,
            this.subarray(start, end),
            targetStart
          );
        }
        return len;
      };
      Buffer3.prototype.fill = function fill(val, start, end, encoding) {
        if (typeof val === "string") {
          if (typeof start === "string") {
            encoding = start;
            start = 0;
            end = this.length;
          } else if (typeof end === "string") {
            encoding = end;
            end = this.length;
          }
          if (encoding !== void 0 && typeof encoding !== "string") {
            throw new TypeError("encoding must be a string");
          }
          if (typeof encoding === "string" && !Buffer3.isEncoding(encoding)) {
            throw new TypeError("Unknown encoding: " + encoding);
          }
          if (val.length === 1) {
            const code = val.charCodeAt(0);
            if (encoding === "utf8" && code < 128 || encoding === "latin1") {
              val = code;
            }
          }
        } else if (typeof val === "number") {
          val = val & 255;
        } else if (typeof val === "boolean") {
          val = Number(val);
        }
        if (start < 0 || this.length < start || this.length < end) {
          throw new RangeError("Out of range index");
        }
        if (end <= start) {
          return this;
        }
        start = start >>> 0;
        end = end === void 0 ? this.length : end >>> 0;
        if (!val) val = 0;
        let i;
        if (typeof val === "number") {
          for (i = start; i < end; ++i) {
            this[i] = val;
          }
        } else {
          const bytes = Buffer3.isBuffer(val) ? val : Buffer3.from(val, encoding);
          const len = bytes.length;
          if (len === 0) {
            throw new TypeError('The value "' + val + '" is invalid for argument "value"');
          }
          for (i = 0; i < end - start; ++i) {
            this[i + start] = bytes[i % len];
          }
        }
        return this;
      };
      var errors = {};
      function E(sym, getMessage, Base) {
        errors[sym] = class NodeError extends Base {
          constructor() {
            super();
            Object.defineProperty(this, "message", {
              value: getMessage.apply(this, arguments),
              writable: true,
              configurable: true
            });
            this.name = `${this.name} [${sym}]`;
            this.stack;
            delete this.name;
          }
          get code() {
            return sym;
          }
          set code(value) {
            Object.defineProperty(this, "code", {
              configurable: true,
              enumerable: true,
              value,
              writable: true
            });
          }
          toString() {
            return `${this.name} [${sym}]: ${this.message}`;
          }
        };
      }
      E(
        "ERR_BUFFER_OUT_OF_BOUNDS",
        function(name) {
          if (name) {
            return `${name} is outside of buffer bounds`;
          }
          return "Attempt to access memory outside buffer bounds";
        },
        RangeError
      );
      E(
        "ERR_INVALID_ARG_TYPE",
        function(name, actual) {
          return `The "${name}" argument must be of type number. Received type ${typeof actual}`;
        },
        TypeError
      );
      E(
        "ERR_OUT_OF_RANGE",
        function(str, range, input) {
          let msg = `The value of "${str}" is out of range.`;
          let received = input;
          if (Number.isInteger(input) && Math.abs(input) > 2 ** 32) {
            received = addNumericalSeparator(String(input));
          } else if (typeof input === "bigint") {
            received = String(input);
            if (input > BigInt(2) ** BigInt(32) || input < -(BigInt(2) ** BigInt(32))) {
              received = addNumericalSeparator(received);
            }
            received += "n";
          }
          msg += ` It must be ${range}. Received ${received}`;
          return msg;
        },
        RangeError
      );
      function addNumericalSeparator(val) {
        let res = "";
        let i = val.length;
        const start = val[0] === "-" ? 1 : 0;
        for (; i >= start + 4; i -= 3) {
          res = `_${val.slice(i - 3, i)}${res}`;
        }
        return `${val.slice(0, i)}${res}`;
      }
      function checkBounds(buf, offset, byteLength2) {
        validateNumber(offset, "offset");
        if (buf[offset] === void 0 || buf[offset + byteLength2] === void 0) {
          boundsError(offset, buf.length - (byteLength2 + 1));
        }
      }
      function checkIntBI(value, min, max, buf, offset, byteLength2) {
        if (value > max || value < min) {
          const n = typeof min === "bigint" ? "n" : "";
          let range;
          if (byteLength2 > 3) {
            if (min === 0 || min === BigInt(0)) {
              range = `>= 0${n} and < 2${n} ** ${(byteLength2 + 1) * 8}${n}`;
            } else {
              range = `>= -(2${n} ** ${(byteLength2 + 1) * 8 - 1}${n}) and < 2 ** ${(byteLength2 + 1) * 8 - 1}${n}`;
            }
          } else {
            range = `>= ${min}${n} and <= ${max}${n}`;
          }
          throw new errors.ERR_OUT_OF_RANGE("value", range, value);
        }
        checkBounds(buf, offset, byteLength2);
      }
      function validateNumber(value, name) {
        if (typeof value !== "number") {
          throw new errors.ERR_INVALID_ARG_TYPE(name, "number", value);
        }
      }
      function boundsError(value, length, type) {
        if (Math.floor(value) !== value) {
          validateNumber(value, type);
          throw new errors.ERR_OUT_OF_RANGE(type || "offset", "an integer", value);
        }
        if (length < 0) {
          throw new errors.ERR_BUFFER_OUT_OF_BOUNDS();
        }
        throw new errors.ERR_OUT_OF_RANGE(
          type || "offset",
          `>= ${type ? 1 : 0} and <= ${length}`,
          value
        );
      }
      var INVALID_BASE64_RE = /[^+/0-9A-Za-z-_]/g;
      function base64clean(str) {
        str = str.split("=")[0];
        str = str.trim().replace(INVALID_BASE64_RE, "");
        if (str.length < 2) return "";
        while (str.length % 4 !== 0) {
          str = str + "=";
        }
        return str;
      }
      function utf8ToBytes(string, units) {
        units = units || Infinity;
        let codePoint;
        const length = string.length;
        let leadSurrogate = null;
        const bytes = [];
        for (let i = 0; i < length; ++i) {
          codePoint = string.charCodeAt(i);
          if (codePoint > 55295 && codePoint < 57344) {
            if (!leadSurrogate) {
              if (codePoint > 56319) {
                if ((units -= 3) > -1) bytes.push(239, 191, 189);
                continue;
              } else if (i + 1 === length) {
                if ((units -= 3) > -1) bytes.push(239, 191, 189);
                continue;
              }
              leadSurrogate = codePoint;
              continue;
            }
            if (codePoint < 56320) {
              if ((units -= 3) > -1) bytes.push(239, 191, 189);
              leadSurrogate = codePoint;
              continue;
            }
            codePoint = (leadSurrogate - 55296 << 10 | codePoint - 56320) + 65536;
          } else if (leadSurrogate) {
            if ((units -= 3) > -1) bytes.push(239, 191, 189);
          }
          leadSurrogate = null;
          if (codePoint < 128) {
            if ((units -= 1) < 0) break;
            bytes.push(codePoint);
          } else if (codePoint < 2048) {
            if ((units -= 2) < 0) break;
            bytes.push(
              codePoint >> 6 | 192,
              codePoint & 63 | 128
            );
          } else if (codePoint < 65536) {
            if ((units -= 3) < 0) break;
            bytes.push(
              codePoint >> 12 | 224,
              codePoint >> 6 & 63 | 128,
              codePoint & 63 | 128
            );
          } else if (codePoint < 1114112) {
            if ((units -= 4) < 0) break;
            bytes.push(
              codePoint >> 18 | 240,
              codePoint >> 12 & 63 | 128,
              codePoint >> 6 & 63 | 128,
              codePoint & 63 | 128
            );
          } else {
            throw new Error("Invalid code point");
          }
        }
        return bytes;
      }
      function asciiToBytes(str) {
        const byteArray = [];
        for (let i = 0; i < str.length; ++i) {
          byteArray.push(str.charCodeAt(i) & 255);
        }
        return byteArray;
      }
      function utf16leToBytes(str, units) {
        let c, hi, lo;
        const byteArray = [];
        for (let i = 0; i < str.length; ++i) {
          if ((units -= 2) < 0) break;
          c = str.charCodeAt(i);
          hi = c >> 8;
          lo = c % 256;
          byteArray.push(lo);
          byteArray.push(hi);
        }
        return byteArray;
      }
      function base64ToBytes(str) {
        return base64.toByteArray(base64clean(str));
      }
      function blitBuffer(src, dst, offset, length) {
        let i;
        for (i = 0; i < length; ++i) {
          if (i + offset >= dst.length || i >= src.length) break;
          dst[i + offset] = src[i];
        }
        return i;
      }
      function isInstance(obj, type) {
        return obj instanceof type || obj != null && obj.constructor != null && obj.constructor.name != null && obj.constructor.name === type.name;
      }
      function numberIsNaN(obj) {
        return obj !== obj;
      }
      var hexSliceLookupTable = (function() {
        const alphabet = "0123456789abcdef";
        const table = new Array(256);
        for (let i = 0; i < 16; ++i) {
          const i16 = i * 16;
          for (let j = 0; j < 16; ++j) {
            table[i16 + j] = alphabet[i] + alphabet[j];
          }
        }
        return table;
      })();
      function defineBigIntMethod(fn) {
        return typeof BigInt === "undefined" ? BufferBigIntNotDefined : fn;
      }
      function BufferBigIntNotDefined() {
        throw new Error("BigInt not supported");
      }
    }
  });

  // entry.js
  var entry_exports = {};
  __export(entry_exports, {
    default: () => entry_default
  });

  // stubs/rn-background-timer.js
  var rn_background_timer_default = {
    setTimeout: (fn, ms) => setTimeout(fn, ms),
    clearTimeout: (id) => clearTimeout(id),
    setInterval: (fn, ms) => setInterval(fn, ms),
    clearInterval: (id) => clearInterval(id)
  };

  // src/utils/message.ts
  var requestMsg = {
    fail: "\u8BF7\u6C42\u5F02\u5E38\u{1F62E}\uFF0C\u53EF\u4EE5\u591A\u8BD5\u51E0\u6B21\uFF0C\u82E5\u8FD8\u662F\u4E0D\u884C\u5C31\u6362\u4E00\u9996\u5427\u3002\u3002\u3002",
    unachievable: "\u54E6No\u{1F631}...\u63A5\u53E3\u65E0\u6CD5\u8BBF\u95EE\u4E86\uFF01",
    timeout: "\u8BF7\u6C42\u8D85\u65F6",
    // unachievable: '哦No😱...接口无法访问了！已帮你切换到临时接口，重试下看能不能播放吧~',
    notConnectNetwork: "\u65E0\u6CD5\u8FDE\u63A5\u5230\u670D\u52A1\u5668",
    cancelRequest: "\u53D6\u6D88http\u8BF7\u6C42",
    tooManyRequests: "\u670D\u52A1\u5668\u7E41\u5FD9"
  };

  // src/utils/musicSdk/options.js
  var bHh = "624868746c";
  var headers = {
    "User-Agent": "lx-music mobile request",
    [bHh]: [bHh]
  };

  // src/utils/request.js
  init_pako();
  var defaultHeaders = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36"
  };
  var httpFetch = (url, options = { method: "get" }) => {
    const requestObj = fetchData(url, options);
    return {
      promise: requestObj.request.catch((err) => {
        console.log("\u51FA\u9519", err.message);
        switch (err.message) {
          case "socket hang up":
            return Promise.reject(new Error(requestMsg.unachievable));
          case "Aborted":
            return Promise.reject(new Error(requestMsg.timeout));
          case "Network request failed":
            return Promise.reject(new Error(requestMsg.notConnectNetwork));
          default:
            return Promise.reject(err);
        }
      }),
      cancelHttp() {
        requestObj.abort();
      }
    };
  };
  var handleDeflateRaw = (data) => new Promise((resolve, reject) => {
    resolve(Buffer.from(deflateRaw(data)));
  });
  var regx = /(?:\d\w)+/g;
  var handleRequestData = async (url, {
    method = "get",
    headers: headers2 = {},
    format = "json",
    cache = "no-store",
    ...options
  }) => {
    headers2 = Object.assign({
      Accept: "application/json"
    }, headers2);
    options.cache = cache;
    if (method.toLocaleLowerCase() === "post" && !headers2["Content-Type"]) {
      if (options.form) {
        headers2["Content-Type"] = "application/x-www-form-urlencoded";
        const formBody = [];
        for (let [key, value] of Object.entries(options.form)) {
          let encodedKey = encodeURIComponent(key);
          let encodedValue = encodeURIComponent(value);
          formBody.push(`${encodedKey}=${encodedValue}`);
        }
        options.body = formBody.join("&");
        delete options.form;
      } else if (options.formData) {
        headers2["Content-Type"] = "multipart/form-data";
        const formBody = [];
        for (let [key, value] of Object.entries(options.form)) {
          let encodedKey = encodeURIComponent(key);
          let encodedValue = encodeURIComponent(value);
          formBody.push(`${encodedKey}=${encodedValue}`);
        }
        options.body = options.formData;
        delete options.formData;
      } else {
        headers2["Content-Type"] = "application/json";
      }
    }
    if (headers2["Content-Type"] === "application/json" && options.body) {
      options.body = JSON.stringify(options.body);
    }
    if (headers2[bHh]) {
      let s = Buffer.from(bHh, "hex").toString();
      s = s.replace(s.substr(-1), "");
      s = Buffer.from(s, "base64").toString();
      const v = process.versions.app.split("-")[0].split(".").map((n) => n.length < 3 ? n.padStart(3, "0") : n).join("");
      const v2 = process.versions.app.split("-")[1] || "";
      headers2[s] = !s || `${(await handleDeflateRaw(Buffer.from(JSON.stringify(`${url}${v}`.match(regx), null, 1).concat(v)).toString("base64"))).toString("hex")}&${parseInt(v)}${v2}`;
      delete headers2[bHh];
    }
    return {
      ...options,
      method,
      headers: Object.assign({}, defaultHeaders, headers2)
    };
  };
  var blobToBuffer = (blob) => {
    return new Promise((resolve, reject) => {
      const reader = new global.FileReader();
      reader.onerror = reject;
      reader.onload = () => {
        const data = reader.result.slice(reader.result.indexOf("base64,") + 7);
        resolve(Buffer.from(data, "base64"));
      };
      reader.readAsDataURL(blob);
    });
  };
  var fetchData = (url, { timeout = 15e3, ...options }) => {
    console.log("---start---", url);
    const controller = new global.AbortController();
    let id = rn_background_timer_default.setTimeout(() => {
      id = null;
      controller.abort();
    }, timeout);
    return {
      request: handleRequestData(url, options).then((options2) => {
        return global.fetch(url, {
          ...options2,
          signal: controller.signal
        }).then((resp) => (options2.binary ? resp.blob() : resp.text()).then((text) => {
          return {
            headers: resp.headers.map,
            body: text,
            statusCode: resp.status,
            statusMessage: resp.statusText,
            url: resp.url,
            ok: resp.ok
          };
        })).then((resp) => {
          if (options2.binary) {
            return blobToBuffer(resp.body).then((buffer) => {
              resp.body = buffer;
              return resp;
            });
          } else {
            try {
              resp.body = JSON.parse(resp.body);
            } catch {
            }
            return resp;
          }
        }).catch((err) => {
          return Promise.reject(err);
        }).finally(() => {
          if (id == null) return;
          rn_background_timer_default.clearTimeout(id);
        });
      }),
      abort() {
        controller.abort();
      }
    };
  };

  // src/utils/musicSdk/kw/tipSearch.js
  var tipSearch_default = {
    regExps: {
      relWord: /RELWORD=(.+)/
    },
    requestObj: null,
    async tipSearchBySong(str) {
      this.cancelTipSearch();
      this.requestObj = httpFetch(`https://tips.kuwo.cn/t.s?corp=kuwo&newver=3&p2p=1&notrace=0&c=mbox&w=${encodeURIComponent(str)}&encoding=utf8&rformat=json`, {
        Referer: "http://www.kuwo.cn/"
      });
      return this.requestObj.promise.then(({ body, statusCode }) => {
        if (statusCode != 200 || !body.WORDITEMS) return Promise.reject(new Error("\u8BF7\u6C42\u5931\u8D25"));
        return body.WORDITEMS;
      });
    },
    handleResult(rawData) {
      return rawData.map((item) => item.RELWORD);
    },
    cancelTipSearch() {
      if (this.requestObj && this.requestObj.cancelHttp) this.requestObj.cancelHttp();
    },
    async search(str) {
      return this.tipSearchBySong(str).then((result) => this.handleResult(result));
    }
  };

  // src/utils/common.ts
  var sizeFormate = (size) => {
    if (!size) return "0 B";
    let units = ["B", "KiB", "MiB", "GiB", "TiB"];
    let number = Math.floor(Math.log(size) / Math.log(1024));
    return `${(size / Math.pow(1024, Math.floor(number))).toFixed(2)} ${units[number]}`;
  };
  var toDateObj = (date) => {
    if (!date) return "";
    switch (typeof date) {
      case "string":
        if (!date.includes("T")) date = date.split(".")[0].replace(/-/g, "/");
      // eslint-disable-next-line no-fallthrough
      case "number":
        date = new Date(date);
      // eslint-disable-next-line no-fallthrough
      case "object":
        break;
      default:
        return "";
    }
    return date;
  };
  var numFix = (n) => n < 10 ? `0${n}` : n.toString();
  var dateFormat = (_date, format = "Y-M-D h:m:s") => {
    const date = toDateObj(_date);
    if (!date) return "";
    return format.replace("Y", date.getFullYear().toString()).replace("M", numFix(date.getMonth() + 1)).replace("D", numFix(date.getDate())).replace("h", numFix(date.getHours())).replace("m", numFix(date.getMinutes())).replace("s", numFix(date.getSeconds()));
  };
  var formatPlayTime = (time) => {
    let m = Math.trunc(time / 60);
    let s = Math.trunc(time % 60);
    return m == 0 && s == 0 ? "--/--" : numFix(m) + ":" + numFix(s);
  };

  // stubs/he.js
  var he_default = decode;
  var decode = decode;

  // src/utils/index.ts
  var dateFormat2 = (time) => {
    let differ = Math.trunc((Date.now() - time) / 1e3);
    if (differ < 60) {
      return global.i18n.t("date_format_second", { num: differ });
    } else if (differ < 3600) {
      return global.i18n.t("date_format_minute", { num: Math.trunc(differ / 60) });
    } else if (differ < 86400) {
      return global.i18n.t("date_format_hour", { num: Math.trunc(differ / 3600) });
    } else {
      return dateFormat(time);
    }
  };
  var formatPlayCount = (num) => {
    if (num > 1e8) return `${Math.trunc(num / 1e7) / 10}\u4EBF`;
    if (num > 1e4) return `${Math.trunc(num / 1e3) / 10}\u4E07`;
    return String(num);
  };
  var decodeName = (str = "") => {
    if (!str) return "";
    return he_default.decode(str);
  };

  // native-stub.js
  var stringMd5 = (str) => globalThis.__lxNative?.md5(str) || "";
  var btoa = (str) => globalThis.__lxNative?.btoa(str) || "";
  var AES_MODE = {
    CBC_128_PKCS7Padding: "AES/CBC/PKCS7Padding",
    ECB_128_NoPadding: "AES"
  };
  var RSA_PADDING = {
    OAEPWithSHA1AndMGF1Padding: "RSA/ECB/OAEPWithSHA1AndMGF1Padding",
    NoPadding: "RSA/ECB/NoPadding"
  };
  var aesEncryptSync = (text, key, iv2, mode) => globalThis.__lxNative?.aesEncrypt(text, key, iv2, mode) || "";
  var aesDecryptSync = (text, key, iv2, mode) => globalThis.__lxNative?.aesDecrypt(text, key, iv2, mode) || "";
  var rsaEncryptSync = (text, key, padding) => globalThis.__lxNative?.rsaEncrypt(text, key, padding) || "";
  var hashSHA1 = async (text) => globalThis.__lxNative?.sha1(text) || "";

  // src/utils/musicSdk/utils.js
  var toMD5 = (str) => stringMd5(str);
  var formatSingerName = (singers, nameKey = "name", join = "\u3001") => {
    if (Array.isArray(singers)) {
      const singer = [];
      singers.forEach((item) => {
        let name = item[nameKey];
        if (!name) return;
        singer.push(name);
      });
      return decodeName(singer.join(join));
    }
    return decodeName(String(singers ?? ""));
  };

  // src/utils/musicSdk/kw/decodeLyric.js
  var { inflate: inflate2 } = (init_pako(), __toCommonJS(pako_exports));
  var handleInflate = (data) => new Promise((resolve, reject) => {
    resolve(Buffer.from(inflate2(data)));
  });
  var buf_key = Buffer.from("yeelion");
  var buf_key_len = buf_key.length;
  var decodeLyric = async (rawData, isGetLyricx) => {
    const buf = Buffer.isBuffer(rawData) ? rawData : Buffer.from(rawData);
    if (buf.toString("utf8", 0, 10).toLowerCase() !== "tp=content") return "";
    const lrcData = await handleInflate(buf.subarray(buf.indexOf("\r\n\r\n") + 4));
    if (!isGetLyricx) return lrcData.toString("utf8");
    const buf_str = Buffer.from(lrcData.toString(), "base64");
    const buf_str_len = buf_str.length;
    const output = new Uint8Array(buf_str_len);
    let i = 0;
    while (i < buf_str_len) {
      let j = 0;
      while (j < buf_key_len && i < buf_str_len) {
        output[i] = buf_str[i] ^ buf_key[j];
        i++;
        j++;
      }
    }
    return Buffer.from(output).toString("utf8");
  };
  var decodeLyric_default = async ({ lrcBuffer, isGetLyricx }) => {
    const lrc = await decodeLyric(lrcBuffer, isGetLyricx);
    return lrc;
  };

  // src/utils/musicSdk/kw/util.js
  var objStr2JSON = (str) => {
    return JSON.parse(str.replace(/('(?=(,\s*')))|('(?=:))|((?<=([:,]\s*))')|((?<={)')|('(?=}))/g, '"'));
  };
  var formatSinger = (rawData) => rawData.replace(/&/g, "\u3001");
  var lrcTools = {
    rxps: {
      wordLine: /^(\[\d{1,2}:.*\d{1,4}\])\s*(\S+(?:\s+\S+)*)?\s*/,
      tagLine: /\[(ver|ti|ar|al|offset|by|kuwo):\s*(\S+(?:\s+\S+)*)\s*\]/,
      wordTimeAll: /<(-?\d+),(-?\d+)(?:,-?\d+)?>/g,
      wordTime: /<(-?\d+),(-?\d+)(?:,-?\d+)?>/
    },
    offset: 1,
    offset2: 1,
    isOK: false,
    lines: [],
    tags: [],
    getWordInfo(str, str2, prevWord) {
      const offset = parseInt(str);
      const offset2 = parseInt(str2);
      let startTime = Math.trunc(Math.abs((offset + offset2) / (this.offset * 2)));
      let endTime = Math.trunc(Math.abs((offset - offset2) / (this.offset2 * 2)) + startTime);
      if (prevWord) {
        if (startTime < prevWord.endTime) {
          prevWord.endTime = startTime;
          if (prevWord.startTime > prevWord.endTime) {
            prevWord.startTime = prevWord.endTime;
          }
          prevWord.newTimeStr = `<${prevWord.startTime},${Math.trunc(prevWord.endTime - prevWord.startTime)}>`;
        }
      }
      return {
        startTime,
        endTime,
        timeStr: `<${startTime},${Math.trunc(endTime - startTime)}>`
      };
    },
    parseLine(line) {
      if (line.length < 6) return;
      let result = this.rxps.wordLine.exec(line);
      if (result) {
        const time = result[1];
        let words = result[2];
        if (words == null) {
          words = "";
        }
        const wordTimes = words.match(this.rxps.wordTimeAll);
        if (!wordTimes) return;
        let preTimeInfo;
        for (const timeStr of wordTimes) {
          const result2 = this.rxps.wordTime.exec(timeStr);
          const wordInfo = this.getWordInfo(result2[1], result2[2], preTimeInfo);
          words = words.replace(timeStr, wordInfo.timeStr);
          if (preTimeInfo?.newTimeStr) words = words.replace(preTimeInfo.timeStr, preTimeInfo.newTimeStr);
          preTimeInfo = wordInfo;
        }
        this.lines.push(time + words);
        return;
      }
      result = this.rxps.tagLine.exec(line);
      if (!result) return;
      if (result[1] == "kuwo") {
        let content = result[2];
        if (content != null && content.includes("][")) {
          content = content.substring(0, content.indexOf("]["));
        }
        const valueOf = parseInt(content, 8);
        this.offset = Math.trunc(valueOf / 10);
        this.offset2 = Math.trunc(valueOf % 10);
        if (this.offset == 0 || Number.isNaN(this.offset) || this.offset2 == 0 || Number.isNaN(this.offset2)) {
          this.isOK = false;
        }
      } else {
        this.tags.push(line);
      }
    },
    parse(lrc) {
      const lines = lrc.split(/\r\n|\r|\n/);
      const tools = Object.create(this);
      tools.isOK = true;
      tools.offset = 1;
      tools.offset2 = 1;
      tools.lines = [];
      tools.tags = [];
      for (const line of lines) {
        if (!tools.isOK) throw new Error("failed");
        tools.parseLine(line);
      }
      if (!tools.lines.length) return "";
      let lrcs = tools.lines.join("\n");
      if (tools.tags.length) lrcs = `${tools.tags.join("\n")}
${lrcs}`;
      return lrcs;
    }
  };
  var wbdCrypto = {
    aesMode: "aes-128-ecb",
    // aesKey: Buffer.from([112, 87, 39, 61, 199, 250, 41, 191, 57, 68, 45, 114, 221, 94, 140, 228], 'binary'),
    aesKey: "cFcnPcf6Kb85RC1y3V6M5A==",
    aesIv: "",
    appId: "y67sprxhhpws",
    decodeData(base64Result) {
      const data = decodeURIComponent(base64Result);
      return JSON.parse(aesDecryptSync(data, this.aesKey, this.aesIv, AES_MODE.ECB_128_NoPadding));
    },
    createSign(data, time) {
      const str = `${this.appId}${data}${time}`;
      return toMD5(str).toUpperCase();
    },
    buildParam(jsonData) {
      const data = Buffer.from(JSON.stringify(jsonData)).toString("base64");
      const time = Date.now();
      const encodeData = aesEncryptSync(data, this.aesKey, this.aesIv, AES_MODE.ECB_128_NoPadding);
      const sign = this.createSign(encodeData, time);
      return `data=${encodeURIComponent(encodeData)}&time=${time}&appId=${this.appId}&sign=${sign}`;
    }
  };

  // src/utils/musicSdk/kw/musicSearch.js
  var musicSearch_default = {
    regExps: {
      mInfo: /level:(\w+),bitrate:(\d+),format:(\w+),size:([\w.]+)/
    },
    limit: 30,
    total: 0,
    page: 0,
    allPage: 1,
    // cancelFn: null,
    musicSearch(str, page, limit) {
      const musicSearchRequestObj = httpFetch(`http://search.kuwo.cn/r.s?client=kt&all=${encodeURIComponent(str)}&pn=${page - 1}&rn=${limit}&uid=794762570&ver=kwplayer_ar_9.2.2.1&vipver=1&show_copyright_off=1&newver=1&ft=music&cluster=0&strategy=2012&encoding=utf8&rformat=json&vermerge=1&mobi=1&issubtitle=1`);
      return musicSearchRequestObj.promise;
    },
    // getImg(songId) {
    //   return httpGet(`http://player.kuwo.cn/webmusic/sj/dtflagdate?flag=6&rid=MUSIC_${songId}`)
    // },
    // getLrc(songId) {
    //   return httpGet(`http://mobile.kuwo.cn/mpage/html5/songinfoandlrc?mid=${songId}&flag=0`)
    // },
    handleResult(rawData) {
      const result = [];
      if (!rawData) return result;
      for (let i = 0; i < rawData.length; i++) {
        const info = rawData[i];
        let songId = info.MUSICRID.replace("MUSIC_", "");
        if (!info.N_MINFO) {
          console.log("N_MINFO is undefined");
          return null;
        }
        const types = [];
        const _types = {};
        let infoArr = info.N_MINFO.split(";");
        for (let info2 of infoArr) {
          info2 = info2.match(this.regExps.mInfo);
          if (info2) {
            switch (info2[2]) {
              case "4000":
                types.push({ type: "flac24bit", size: info2[4] });
                _types.flac24bit = {
                  size: info2[4].toLocaleUpperCase()
                };
                break;
              case "2000":
                types.push({ type: "flac", size: info2[4] });
                _types.flac = {
                  size: info2[4].toLocaleUpperCase()
                };
                break;
              case "320":
                types.push({ type: "320k", size: info2[4] });
                _types["320k"] = {
                  size: info2[4].toLocaleUpperCase()
                };
                break;
              case "128":
                types.push({ type: "128k", size: info2[4] });
                _types["128k"] = {
                  size: info2[4].toLocaleUpperCase()
                };
                break;
            }
          }
        }
        types.reverse();
        let interval = parseInt(info.DURATION);
        result.push({
          name: decodeName(info.SONGNAME),
          singer: formatSinger(decodeName(info.ARTIST)),
          source: "kw",
          // img = (info.album.name === '' || info.album.name === '空')
          //   ? `http://player.kuwo.cn/webmusic/sj/dtflagdate?flag=6&rid=MUSIC_160911.jpg`
          //   : `https://y.gtimg.cn/music/photo_new/T002R500x500M000${info.album.mid}.jpg`
          songmid: songId,
          albumId: decodeName(info.ALBUMID || ""),
          interval: Number.isNaN(interval) ? 0 : formatPlayTime(interval),
          albumName: info.ALBUM ? decodeName(info.ALBUM) : "",
          lrc: null,
          img: null,
          otherSource: null,
          types,
          _types,
          typeUrl: {}
        });
      }
      return result;
    },
    search(str, page = 1, limit, retryNum = 0) {
      if (retryNum > 2) return Promise.reject(new Error("try max num"));
      if (limit == null) limit = this.limit;
      return this.musicSearch(str, page, limit).then(({ body: result }) => {
        if (!result || result.TOTAL !== "0" && result.SHOW === "0") return this.search(str, page, limit, ++retryNum);
        let list = this.handleResult(result.abslist);
        if (list == null) return this.search(str, page, limit, ++retryNum);
        this.total = parseInt(result.TOTAL);
        this.page = page;
        this.allPage = Math.ceil(this.total / limit);
        return Promise.resolve({
          list,
          allPage: this.allPage,
          total: this.total,
          limit,
          source: "kw"
        });
      });
    }
  };

  // src/utils/musicSdk/kw/leaderboard.js
  var boardList = [{ id: "kw__93", name: "\u98D9\u5347\u699C", bangid: "93" }, { id: "kw__17", name: "\u65B0\u6B4C\u699C", bangid: "17" }, { id: "kw__16", name: "\u70ED\u6B4C\u699C", bangid: "16" }, { id: "kw__158", name: "\u6296\u97F3\u70ED\u6B4C\u699C", bangid: "158" }, { id: "kw__292", name: "\u94C3\u58F0\u699C", bangid: "292" }, { id: "kw__284", name: "\u70ED\u8BC4\u699C", bangid: "284" }, { id: "kw__290", name: "ACG\u65B0\u6B4C\u699C", bangid: "290" }, { id: "kw__286", name: "\u53F0\u6E7EKKBOX\u699C", bangid: "286" }, { id: "kw__279", name: "\u51AC\u65E5\u6696\u5FC3\u699C", bangid: "279" }, { id: "kw__281", name: "\u5DF4\u58EB\u968F\u8EAB\u542C\u699C", bangid: "281" }, { id: "kw__255", name: "KTV\u70B9\u5531\u699C", bangid: "255" }, { id: "kw__280", name: "\u5BB6\u52A1\u8FDB\u884C\u66F2\u699C", bangid: "280" }, { id: "kw__282", name: "\u71AC\u591C\u4FEE\u4ED9\u699C", bangid: "282" }, { id: "kw__283", name: "\u6795\u8FB9\u8F7B\u97F3\u4E50\u699C", bangid: "283" }, { id: "kw__278", name: "\u53E4\u98CE\u97F3\u4E50\u699C", bangid: "278" }, { id: "kw__264", name: "Vlog\u97F3\u4E50\u699C", bangid: "264" }, { id: "kw__242", name: "\u7535\u97F3\u699C", bangid: "242" }, { id: "kw__187", name: "\u6D41\u884C\u8D8B\u52BF\u699C", bangid: "187" }, { id: "kw__204", name: "\u73B0\u573A\u97F3\u4E50\u699C", bangid: "204" }, { id: "kw__186", name: "ACG\u795E\u66F2\u699C", bangid: "186" }, { id: "kw__185", name: "\u6700\u5F3A\u7FFB\u5531\u699C", bangid: "185" }, { id: "kw__26", name: "\u7ECF\u5178\u6000\u65E7\u699C", bangid: "26" }, { id: "kw__104", name: "\u534E\u8BED\u699C", bangid: "104" }, { id: "kw__182", name: "\u7CA4\u8BED\u699C", bangid: "182" }, { id: "kw__22", name: "\u6B27\u7F8E\u699C", bangid: "22" }, { id: "kw__184", name: "\u97E9\u8BED\u699C", bangid: "184" }, { id: "kw__183", name: "\u65E5\u8BED\u699C", bangid: "183" }, { id: "kw__145", name: "\u4F1A\u5458\u7545\u542C\u699C", bangid: "145" }, { id: "kw__153", name: "\u7F51\u7EA2\u65B0\u6B4C\u699C", bangid: "153" }, { id: "kw__64", name: "\u5F71\u89C6\u91D1\u66F2\u699C", bangid: "64" }, { id: "kw__176", name: "DJ\u55E8\u6B4C\u699C", bangid: "176" }, { id: "kw__106", name: "\u771F\u58F0\u97F3", bangid: "106" }, { id: "kw__12", name: "Billboard\u699C", bangid: "12" }, { id: "kw__49", name: "iTunes\u97F3\u4E50\u699C", bangid: "49" }, { id: "kw__180", name: "beatport\u7535\u97F3\u699C", bangid: "180" }, { id: "kw__13", name: "\u82F1\u56FDUK\u699C", bangid: "13" }, { id: "kw__164", name: "\u767E\u5927DJ\u699C", bangid: "164" }, { id: "kw__246", name: "YouTube\u97F3\u4E50\u6392\u884C\u699C", bangid: "246" }, { id: "kw__265", name: "\u97E9\u56FDGenie\u699C", bangid: "265" }, { id: "kw__14", name: "\u97E9\u56FDM-net\u699C", bangid: "14" }, { id: "kw__8", name: "\u9999\u6E2F\u7535\u53F0\u699C", bangid: "8" }, { id: "kw__15", name: "\u65E5\u672C\u516C\u4FE1\u699C", bangid: "15" }, { id: "kw__151", name: "\u817E\u8BAF\u97F3\u4E50\u4EBA\u539F\u521B\u699C", bangid: "151" }];
  var sortQualityArray = (array) => {
    const qualityMap = {
      flac24bit: 4,
      flac: 3,
      "320k": 2,
      "128k": 1
    };
    const rawQualityArray = [];
    const newQualityArray = [];
    array.forEach((item, index) => {
      const type = qualityMap[item.type];
      if (!type) return;
      rawQualityArray.push({ type, index });
    });
    rawQualityArray.sort((a, b) => a.type - b.type);
    rawQualityArray.forEach((item) => {
      newQualityArray.push(array[item.index]);
    });
    return newQualityArray;
  };
  var leaderboard_default = {
    list: [
      {
        id: "kwbiaosb",
        name: "\u98D9\u5347\u699C",
        bangid: 93
      },
      {
        id: "kwregb",
        name: "\u70ED\u6B4C\u699C",
        bangid: 16
      },
      {
        id: "kwhuiyb",
        name: "\u4F1A\u5458\u699C",
        bangid: 145
      },
      {
        id: "kwdouyb",
        name: "\u6296\u97F3\u699C",
        bangid: 158
      },
      {
        id: "kwqsb",
        name: "\u8D8B\u52BF\u699C",
        bangid: 187
      },
      {
        id: "kwhuaijb",
        name: "\u6000\u65E7\u699C",
        bangid: 26
      },
      {
        id: "kwhuayb",
        name: "\u534E\u8BED\u699C",
        bangid: 104
      },
      {
        id: "kwyueyb",
        name: "\u7CA4\u8BED\u699C",
        bangid: 182
      },
      {
        id: "kwoumb",
        name: "\u6B27\u7F8E\u699C",
        bangid: 22
      },
      {
        id: "kwhanyb",
        name: "\u97E9\u8BED\u699C",
        bangid: 184
      },
      {
        id: "kwriyb",
        name: "\u65E5\u8BED\u699C",
        bangid: 183
      }
    ],
    // getUrl: (p, l, id) => `http://kbangserver.kuwo.cn/ksong.s?from=pc&fmt=json&pn=${p - 1}&rn=${l}&type=bang&data=content&id=${id}&show_copyright_off=0&pcmp4=1&isbang=1`,
    regExps: {
      mInfo: /level:(\w+),bitrate:(\d+),format:(\w+),size:([\w.]+)/
    },
    limit: 100,
    _requestBoardsObj: null,
    getBoardsData() {
      if (this._requestBoardsObj) this._requestBoardsObj.cancelHttp();
      this._requestBoardsObj = httpFetch("http://qukudata.kuwo.cn/q.k?op=query&cont=tree&node=2&pn=0&rn=1000&fmt=json&level=2");
      return this._requestBoardsObj.promise;
    },
    getData(url) {
      const requestDataObj = httpFetch(url);
      return requestDataObj.promise;
    },
    filterData(rawList) {
      return rawList.map((item) => {
        let types = [];
        const _types = {};
        const qualitys = /* @__PURE__ */ new Set();
        item.n_minfo.split(";").forEach((i) => {
          const info = i.match(this.regExps.mInfo);
          if (!info) return;
          const quality = info[2];
          const size = info[4].toLocaleUpperCase();
          if (qualitys.has(quality)) return;
          qualitys.add(quality);
          switch (quality) {
            case "4000":
              types.push({ type: "flac24bit", size });
              _types.flac24bit = { size };
              break;
            case "2000":
              types.push({ type: "flac", size });
              _types.flac = { size };
              break;
            case "320":
              types.push({ type: "320k", size });
              _types["320k"] = { size };
              break;
            case "128":
              types.push({ type: "128k", size });
              _types["128k"] = { size };
              break;
          }
        });
        types = sortQualityArray(types);
        return {
          singer: formatSinger(decodeName(item.artist)),
          name: decodeName(item.name),
          albumName: decodeName(item.album),
          albumId: item.albumId,
          songmid: item.id,
          source: "kw",
          interval: formatPlayTime(parseInt(item.duration)),
          img: item.pic,
          lrc: null,
          otherSource: null,
          types,
          _types,
          typeUrl: {}
        };
      });
    },
    filterBoardsData(rawList) {
      let list = [];
      for (const board of rawList) {
        if (board.source != "1") continue;
        list.push({
          id: "kw__" + board.sourceid,
          name: board.name,
          bangid: String(board.sourceid)
        });
      }
      return list;
    },
    async getBoards(retryNum = 0) {
      this.list = boardList;
      return {
        list: boardList,
        source: "kw"
      };
    },
    getList(id, page, retryNum = 0) {
      if (++retryNum > 3) return Promise.reject(new Error("try max num"));
      const requestBody = { uid: "", devId: "", sFrom: "kuwo_sdk", user_type: "AP", carSource: "kwplayercar_ar_6.0.1.0_apk_keluze.apk", id, pn: page - 1, rn: this.limit };
      const requestUrl = `https://wbd.kuwo.cn/api/bd/bang/bang_info?${wbdCrypto.buildParam(requestBody)}`;
      const request = httpFetch(requestUrl, { cache: "default" }).promise;
      return request.then(({ statusCode, body }) => {
        const rawData = wbdCrypto.decodeData(body);
        const data = rawData.data;
        if (statusCode !== 200 || rawData.code != 200 || !data.musiclist) return this.getList(id, page, retryNum);
        const total = parseInt(data.total);
        const list = this.filterData(data.musiclist);
        return {
          total,
          list,
          limit: this.limit,
          page,
          source: "kw"
        };
      });
    }
    // getDetailPageUrl(id) {
    //   return `http://www.kuwo.cn/rankList/${id}`
    // },
  };

  // src/utils/musicSdk/kw/lyric.js
  var timeExp = /^\[([\d:.]*)\]{1}/g;
  var existTimeExp = /\[\d{1,2}:.*\d{1,4}]/;
  var lyricxTag = /^<-?\d+,-?\d+>/;
  var lyric_default = {
    /* sortLrcArr(arr) {
        const lrcSet = new Set()
        let lrc = []
        let lrcT = []
        let markIndex = []
        for (const item of arr) {
          if (lrcSet.has(item.time)) {
            if (lrc.length < 2) continue
            const index = lrc.findIndex(l => l.time == item.time)
            markIndex.push(index)
            if (index == lrc.length - 1) {
              lrcT.push({ ...lrc[index], time: item.time })
              lrc.push(item)
            } else {
              lrcT.push({ ...lrc[index], time: lrc[index + 1].time })
              if (item.text) {
                //   const lastIndex = lrc.length - 1
                //   markIndex.push(lastIndex)
                //   lrcT.push({ ...lrc[lastIndex], time: lrc[lastIndex - 1].time })
                lrc.push(item)
              }
            }
          } else {
            lrc.push(item)
            lrcSet.add(item.time)
          }
        }
    
        // console.log(markIndex)
        markIndex = Array.from(new Set(markIndex))
        for (let index = markIndex.length - 1; index >= 0; index--) {
          lrc.splice(markIndex[index], 1)
        }
    
        // if (lrcT.length) {
        //   if (lrc.length * 0.4 < lrcT.length) { // 翻译数量需大于歌词数量的0.4倍，否则认为没有翻译
        //     const tItem = lrc.pop()
        //     tItem.time = lrc[lrc.length - 1].time
        //     lrcT.push(tItem)
        //   } else {
        //     lrc = arr
        //     lrcT = []
        //   }
        // }
    
        console.log(lrc, lrcT)
    
        return {
          lrc,
          lrcT,
        }
      }, */
    sortLrcArr(arr) {
      const lrcSet = /* @__PURE__ */ new Set();
      let lrc = [];
      let lrcT = [];
      let isLyricx = false;
      for (const item of arr) {
        if (lrcSet.has(item.time)) {
          if (lrc.length < 2) continue;
          const tItem = lrc.pop();
          tItem.time = lrc[lrc.length - 1].time;
          lrcT.push(tItem);
          lrc.push(item);
        } else {
          lrc.push(item);
          lrcSet.add(item.time);
        }
        if (!isLyricx && lyricxTag.test(item.text)) isLyricx = true;
      }
      if (!isLyricx && lrcT.length > lrc.length * 0.3 && lrc.length - lrcT.length > 6) {
        throw new Error("failed");
      }
      return {
        lrc,
        lrcT
      };
    },
    transformLrc(tags, lrclist) {
      return `${tags.join("\n")}
${lrclist ? lrclist.map((l) => `[${l.time}]${l.text}
`).join("") : "\u6682\u65E0\u6B4C\u8BCD"}`;
    },
    parseLrc(lrc) {
      const lines = lrc.split(/\r\n|\r|\n/);
      let tags = [];
      let lrcArr = [];
      for (let i = 0; i < lines.length; i++) {
        const line = lines[i].trim();
        let result = timeExp.exec(line);
        if (result) {
          const text = line.replace(timeExp, "").trim();
          let time = RegExp.$1;
          if (/\.\d\d$/.test(time)) time += "0";
          lrcArr.push({
            time,
            text
          });
        } else if (lrcTools.rxps.tagLine.test(line)) {
          tags.push(line);
        }
      }
      const lrcInfo = this.sortLrcArr(lrcArr);
      return {
        lyric: decodeName(this.transformLrc(tags, lrcInfo.lrc)),
        tlyric: lrcInfo.lrcT.length ? decodeName(this.transformLrc(tags, lrcInfo.lrcT)) : ""
      };
    },
    // getLyric2(musicInfo, isGetLyricx = true) {
    //   const requestObj = httpFetch(`http://newlyric.kuwo.cn/newlyric.lrc?${buildParams(musicInfo.songmid, isGetLyricx)}`)
    //   requestObj.promise = requestObj.promise.then(({ statusCode, body, raw }) => {
    //     if (statusCode != 200) return Promise.reject(new Error(JSON.stringify(body)))
    //     return decodeLyric({ lrcBase64: raw.toString('base64'), isGetLyricx }).then(base64Data => {
    //       let lrcInfo
    //       console.log(Buffer.from(base64Data, 'base64').toString())
    //       try {
    //         lrcInfo = this.parseLrc(Buffer.from(base64Data, 'base64').toString())
    //       } catch {
    //         return Promise.reject(new Error('Get lyric failed'))
    //       }
    //       if (lrcInfo.tlyric) lrcInfo.tlyric = lrcInfo.tlyric.replace(lrcTools.rxps.wordTimeAll, '')
    //       lrcInfo.lxlyric = lrcTools.parse(lrcInfo.lyric)
    //       // console.log(lrcInfo.lyric)
    //       // console.log(lrcInfo.tlyric)
    //       // console.log(lrcInfo.lxlyric)
    //       // console.log(JSON.stringify(lrcInfo))
    //     })
    //   })
    //   return requestObj
    // },
    getLyric(musicInfo, isGetLyricx = true) {
      const requestObj = httpFetch(`http://mlyric.kuwo.cn/mobi.s?f=web&type=lyric&lrcx=${isGetLyricx ? 1 : 0}&rid=${musicInfo.songmid}&encode=utf8`, {
        cache: false,
        binary: true
      });
      requestObj.promise = requestObj.promise.then(async ({ statusCode, body }) => {
        if (statusCode != 200) return Promise.reject(new Error(JSON.stringify(body)));
        const lrcText = await decodeLyric_default({ lrcBuffer: body, isGetLyricx });
        let lrcInfo;
        try {
          lrcInfo = this.parseLrc(lrcText);
        } catch (err) {
          return Promise.reject(new Error("Get lyric failed"));
        }
        if (lrcInfo.tlyric) lrcInfo.tlyric = lrcInfo.tlyric.replace(lrcTools.rxps.wordTimeAll, "");
        try {
          lrcInfo.lxlyric = lrcTools.parse(lrcInfo.lyric);
        } catch {
          lrcInfo.lxlyric = "";
        }
        lrcInfo.lyric = lrcInfo.lyric.replace(lrcTools.rxps.wordTimeAll, "");
        if (!existTimeExp.test(lrcInfo.lyric)) return Promise.reject(new Error("Get lyric failed"));
        return lrcInfo;
      });
      return requestObj;
    }
  };

  // src/utils/musicSdk/kw/pic.js
  var pic_default = {
    getPic({ songmid }) {
      const requestObj = httpFetch(`http://artistpicserver.kuwo.cn/pic.web?corp=kuwo&type=rid_pic&pictype=500&size=500&rid=${songmid}`);
      requestObj.promise = requestObj.promise.then(({ body }) => /^http/.test(body) ? body : null);
      return requestObj.promise;
    }
  };

  // src/utils/musicSdk/api-source-info.ts
  var sources = [];
  var api_source_info_default = sources;

  // stubs/setting-state.js
  var setting_state_default = new Proxy({ setting: { "common.apiSource": "" } }, {});

  // src/utils/musicSdk/api-source.js
  var apiList = {
    // temp_api_kw,
    // // test_api_bd: require('./bd/api-test'),
    // test_api_kg,
    // test_api_kw,
    // test_api_tx,
    // test_api_wy,
    // test_api_mg,
    // direct_api_kg,
    // direct_api_kw,
    // direct_api_tx,
    // direct_api_wy,
    // direct_api_mg,
    // test_api_tx: require('./tx/api-test'),
    // test_api_wy: require('./wy/api-test'),
    // test_api_xm: require('./xm/api-test'),
  };
  var supportQuality = {};
  for (const api of api_source_info_default) {
    supportQuality[api.id] = api.supportQualitys;
  }
  var getAPI = (source) => apiList[`${setting_state_default.setting["common.apiSource"]}_api_${source}`];
  var apis = (source) => {
    if (/^user_api/.test(setting_state_default.setting["common.apiSource"])) return global.lx.apis[source];
    const api = getAPI(source);
    if (api) return api;
    throw new Error("Api is not found");
  };

  // src/utils/musicSdk/kw/album.js
  var album_default = {
    limit_list: 36,
    limit_song: 1e3,
    filterListDetail(rawList, albumName, albumId) {
      return rawList.map((item, inedx) => {
        let formats = item.formats.split("|");
        let types = [];
        let _types = {};
        if (formats.includes("MP3128")) {
          types.push({ type: "128k", size: null });
          _types["128k"] = {
            size: null
          };
        }
        if (formats.includes("MP3H")) {
          types.push({ type: "320k", size: null });
          _types["320k"] = {
            size: null
          };
        }
        if (formats.includes("ALFLAC")) {
          types.push({ type: "flac", size: null });
          _types.flac = {
            size: null
          };
        }
        if (formats.includes("HIRFLAC")) {
          types.push({ type: "flac24bit", size: null });
          _types.flac24bit = {
            size: null
          };
        }
        return {
          singer: formatSinger(decodeName(item.artist)),
          name: decodeName(item.name),
          albumName,
          albumId,
          songmid: item.id,
          source: "kw",
          interval: null,
          img: item.pic,
          lrc: null,
          otherSource: null,
          types,
          _types,
          typeUrl: {}
        };
      });
    },
    /**
     * 格式化播放数量
     * @param {*} num
     */
    formatPlayCount(num) {
      if (num > 1e8) return parseInt(num / 1e7) / 10 + "\u4EBF";
      if (num > 1e4) return parseInt(num / 1e3) / 10 + "\u4E07";
      return num;
    },
    getAlbumListDetail(id, page, retryNum = 0) {
      if (retryNum > 2) return Promise.reject(new Error("try max num"));
      const requestObj_listDetail = httpFetch(`http://search.kuwo.cn/r.s?pn=${page - 1}&rn=${this.limit_song}&stype=albuminfo&albumid=${id}&show_copyright_off=0&encoding=utf&vipver=MUSIC_9.1.0`);
      return requestObj_listDetail.promise.then(({ statusCode, body }) => {
        if (statusCode !== 200) return this.getAlbumListDetail(id, page, ++retryNum);
        body = objStr2JSON(body);
        if (!body.musiclist) return this.getAlbumListDetail(id, page, ++retryNum);
        body.name = decodeName(body.name);
        return {
          list: this.filterListDetail(body.musiclist, body.name, body.albumid),
          page,
          limit: this.limit_song,
          total: parseInt(body.songnum),
          source: "kw",
          info: {
            name: body.name,
            img: body.img || body.hts_img,
            desc: decodeName(body.info),
            author: decodeName(body.artist)
            // play_count: this.formatPlayCount(body.playnum),
          }
        };
      });
    }
    // getAlbumListDetail(id, page, retryNum = 0) {
    //   if (retryNum > 2) return Promise.reject(new Error('try max num'))
    //   return tokenRequest(`http://www.kuwo.cn/api/www/album/albumInfo?albumId=${id}&pn=${page}&rn=${this.limit_song}&httpsStatus=1`).then((resp) => {
    //     return resp.promise.then(({ statusCode, body }) => {
    //       console.log(body)
    //       return Promise.reject(new Error('failed'))
    //       // if (statusCode !== 200) return this.getAlbumListDetail(id, page, ++retryNum)
    //       // const data = body.data
    //       // console.log(data)
    //       // if (!data.musicList) return this.getAlbumListDetail(id, page, ++retryNum)
    //       // return {
    //       //   list: this.filterListDetail(data.musiclist),
    //       //   page,
    //       //   limit: this.limit_song,
    //       //   total: data.total,
    //       //   source: 'kw',
    //       //   info: {
    //       //     name: data.album,
    //       //     img: data.pic,
    //       //     desc: data.albuminfo,
    //       //     author: data.artist,
    //       //     play_count: this.formatPlayCount(data.playCnt),
    //       //   },
    //       // }
    //     })
    //   })
    // },
  };

  // src/utils/musicSdk/kw/songList.js
  var songList_default = {
    _requestObj_tags: null,
    _requestObj_hotTags: null,
    _requestObj_list: null,
    limit_list: 36,
    limit_song: 1e3,
    successCode: 200,
    sortList: [
      {
        name: "\u6700\u65B0",
        tid: "new",
        id: "new"
      },
      {
        name: "\u6700\u70ED",
        tid: "hot",
        id: "hot"
      }
    ],
    regExps: {
      mInfo: /level:(\w+),bitrate:(\d+),format:(\w+),size:([\w.]+)/,
      // http://www.kuwo.cn/playlist_detail/2886046289
      // https://m.kuwo.cn/h5app/playlist/2736267853?t=qqfriend
      listDetailLink: /^.+\/playlist(?:_detail)?\/(\d+)(?:\?.*|&.*$|#.*$|$)/
    },
    tagsUrl: "http://wapi.kuwo.cn/api/pc/classify/playlist/getTagList?cmd=rcm_keyword_playlist&user=0&prod=kwplayer_pc_9.0.5.0&vipver=9.0.5.0&source=kwplayer_pc_9.0.5.0&loginUid=0&loginSid=0&appUid=76039576",
    hotTagUrl: "http://wapi.kuwo.cn/api/pc/classify/playlist/getRcmTagList?loginUid=0&loginSid=0&appUid=76039576",
    getListUrl({ sortId, id, type, page }) {
      if (!id) return `http://wapi.kuwo.cn/api/pc/classify/playlist/getRcmPlayList?loginUid=0&loginSid=0&appUid=76039576&&pn=${page}&rn=${this.limit_list}&order=${sortId}`;
      switch (type) {
        case "10000":
          return `http://wapi.kuwo.cn/api/pc/classify/playlist/getTagPlayList?loginUid=0&loginSid=0&appUid=76039576&pn=${page}&id=${id}&rn=${this.limit_list}`;
        case "43":
          return `http://mobileinterfaces.kuwo.cn/er.s?type=get_pc_qz_data&f=web&id=${id}&prod=pc`;
      }
    },
    getListDetailUrl(id, page) {
      return `http://nplserver.kuwo.cn/pl.svc?op=getlistinfo&pid=${id}&pn=${page - 1}&rn=${this.limit_song}&encode=utf8&keyset=pl2012&identity=kuwo&pcmp4=1&vipver=MUSIC_9.0.5.0_W1&newver=1`;
    },
    // http://nplserver.kuwo.cn/pl.svc?op=getlistinfo&pid=2849349915&pn=0&rn=100&encode=utf8&keyset=pl2012&identity=kuwo&pcmp4=1&vipver=MUSIC_9.0.5.0_W1&newver=1
    // 获取标签
    getTag(tryNum = 0) {
      if (this._requestObj_tags) this._requestObj_tags.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      this._requestObj_tags = httpFetch(this.tagsUrl);
      return this._requestObj_tags.promise.then(({ body }) => {
        if (body.code !== this.successCode) return this.getTag(++tryNum);
        return this.filterTagInfo(body.data);
      });
    },
    // 获取标签
    getHotTag(tryNum = 0) {
      if (this._requestObj_hotTags) this._requestObj_hotTags.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      this._requestObj_hotTags = httpFetch(this.hotTagUrl);
      return this._requestObj_hotTags.promise.then(({ body }) => {
        if (body.code !== this.successCode) return this.getHotTag(++tryNum);
        return this.filterInfoHotTag(body.data[0].data);
      });
    },
    filterInfoHotTag(rawList) {
      return rawList.map((item) => ({
        id: `${item.id}-${item.digest}`,
        name: item.name,
        source: "kw"
      }));
    },
    filterTagInfo(rawList) {
      return rawList.map((type) => ({
        name: type.name,
        list: type.data.map((item) => ({
          parent_id: type.id,
          parent_name: type.name,
          id: `${item.id}-${item.digest}`,
          name: item.name,
          source: "kw"
        }))
      }));
    },
    // 获取列表数据
    getList(sortId, tagId, page, tryNum = 0) {
      if (this._requestObj_list) this._requestObj_list.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      let id;
      let type;
      if (tagId) {
        let arr = tagId.split("-");
        id = arr[0];
        type = arr[1];
      } else {
        id = null;
      }
      this._requestObj_list = httpFetch(this.getListUrl({ sortId, id, type, page }));
      return this._requestObj_list.promise.then(({ body }) => {
        if (!id || type == "10000") {
          if (body.code !== this.successCode) return this.getList(sortId, tagId, page, ++tryNum);
          return {
            list: this.filterList(body.data.data),
            total: body.data.total,
            page: body.data.pn,
            limit: body.data.rn,
            source: "kw"
          };
        } else if (!body.length) {
          return this.getList(sortId, tagId, page, ++tryNum);
        }
        return {
          list: this.filterList2(body),
          total: 1e3,
          page,
          limit: 1e3,
          source: "kw"
        };
      });
    },
    /**
     * 格式化播放数量
     * @param {*} num
     */
    formatPlayCount(num) {
      if (num > 1e8) return parseInt(num / 1e7) / 10 + "\u4EBF";
      if (num > 1e4) return parseInt(num / 1e3) / 10 + "\u4E07";
      return num;
    },
    filterList(rawData) {
      return rawData.map((item) => ({
        play_count: this.formatPlayCount(item.listencnt),
        id: `digest-${item.digest}__${item.id}`,
        author: item.uname,
        name: item.name,
        // time: item.publish_time,
        total: item.total,
        img: item.img,
        grade: item.favorcnt / 10,
        desc: item.desc,
        source: "kw"
      }));
    },
    filterList2(rawData) {
      const list = [];
      const allowedTypes = ["songlist", "list", "album"];
      rawData.forEach((item) => {
        item.list.forEach((item2) => {
          if (!allowedTypes.includes(item2.type)) return;
          list.push({
            play_count: item2.play_count && this.formatPlayCount(item2.listencnt),
            id: `digest-${item2.digest}__${item2.id}`,
            author: item2.uname,
            name: item2.name,
            total: item2.total,
            // time: item.publish_time,
            img: item2.img,
            grade: item2.favorcnt && item2.favorcnt / 10,
            desc: item2.desc,
            source: "kw"
          });
        });
      });
      return list;
    },
    getListDetailDigest8(id, page, tryNum = 0) {
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      const requestObj = httpFetch(this.getListDetailUrl(id, page));
      return requestObj.promise.then(({ body }) => {
        if (body.result !== "ok") return this.getListDetail(id, page, ++tryNum);
        return {
          list: this.filterListDetail(body.musiclist),
          page,
          limit: body.rn,
          total: body.total,
          source: "kw",
          info: {
            name: body.title,
            img: body.pic,
            desc: body.info,
            author: body.uname,
            play_count: this.formatPlayCount(body.playnum)
          }
        };
      });
    },
    getListDetailDigest5Info(id, tryNum = 0) {
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      const requestObj = httpFetch(`http://qukudata.kuwo.cn/q.k?op=query&cont=ninfo&node=${id}&pn=0&rn=1&fmt=json&src=mbox&level=2`);
      return requestObj.promise.then(({ statusCode, body }) => {
        if (statusCode != 200 || !body.child) return this.getListDetail(id, ++tryNum);
        return body.child.length ? body.child[0].sourceid : null;
      });
    },
    getListDetailDigest5Music(id, page, tryNum = 0) {
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      const requestObj = httpFetch(`http://nplserver.kuwo.cn/pl.svc?op=getlistinfo&pid=${id}&pn=${page - 1}}&rn=${this.limit_song}&encode=utf-8&keyset=pl2012&identity=kuwo&pcmp4=1`);
      return requestObj.promise.then(({ body }) => {
        if (body.result !== "ok") return this.getListDetail(id, page, ++tryNum);
        return {
          list: this.filterListDetail(body.musiclist),
          page,
          limit: body.rn,
          total: body.total,
          source: "kw",
          info: {
            name: body.title,
            img: body.pic,
            desc: body.info,
            author: body.uname,
            play_count: this.formatPlayCount(body.playnum)
          }
        };
      });
    },
    async getListDetailDigest5(id, page, retryNum) {
      const detailId = await this.getListDetailDigest5Info(id, retryNum);
      return this.getListDetailDigest5Music(detailId, page, retryNum);
    },
    filterBDListDetail(rawList) {
      return rawList.map((item) => {
        let types = [];
        let _types = {};
        for (let info of item.audios) {
          info.size = info.size?.toLocaleUpperCase();
          switch (info.bitrate) {
            case "4000":
              types.push({ type: "flac24bit", size: info.size });
              _types.flac24bit = {
                size: info.size
              };
              break;
            case "2000":
              types.push({ type: "flac", size: info.size });
              _types.flac = {
                size: info.size
              };
              break;
            case "320":
              types.push({ type: "320k", size: info.size });
              _types["320k"] = {
                size: info.size
              };
              break;
            case "128":
              types.push({ type: "128k", size: info.size });
              _types["128k"] = {
                size: info.size
              };
              break;
          }
        }
        types.reverse();
        return {
          singer: item.artists.map((s) => s.name).join("\u3001"),
          name: item.name,
          albumName: item.album,
          albumId: item.albumId,
          songmid: item.id,
          source: "kw",
          interval: formatPlayTime(item.duration),
          img: item.albumPic,
          releaseDate: item.releaseDate,
          lrc: null,
          otherSource: null,
          types,
          _types,
          typeUrl: {}
        };
      });
    },
    getReqId() {
      function t() {
        return (65536 * (1 + Math.random()) | 0).toString(16).substring(1);
      }
      return t() + t() + t() + t() + t() + t() + t() + t();
    },
    async getListDetailMusicListByBDListInfo(id, source) {
      const { body: infoData } = await httpFetch(`https://bd-api.kuwo.cn/api/service/playlist/info/${id}?reqId=${this.getReqId()}&source=${source}`, {
        headers: {
          "User-Agent": "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36",
          plat: "h5"
        }
      }).promise.catch(() => ({ code: 0 }));
      if (infoData.code != 200) return null;
      return {
        name: infoData.data.name,
        img: infoData.data.pic,
        desc: infoData.data.description,
        author: infoData.data.creatorName,
        play_count: infoData.data.playNum
      };
    },
    async getListDetailMusicListByBDUserPub(id) {
      const { body: infoData } = await httpFetch(`https://bd-api.kuwo.cn/api/ucenter/users/pub/${id}?reqId=${this.getReqId()}`, {
        headers: {
          "User-Agent": "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36",
          plat: "h5"
        }
      }).promise.catch(() => ({ code: 0 }));
      if (infoData.code != 200) return null;
      return {
        name: infoData.data.userInfo.nickname + "\u559C\u6B22\u7684\u97F3\u4E50",
        img: infoData.data.userInfo.headImg,
        desc: "",
        author: infoData.data.userInfo.nickname,
        play_count: ""
      };
    },
    async getListDetailMusicListByBDList(id, source, page, tryNum = 0) {
      const { body: listData } = await httpFetch(`https://bd-api.kuwo.cn/api/service/playlist/${id}/musicList?reqId=${this.getReqId()}&source=${source}&pn=${page}&rn=${this.limit_song}`, {
        headers: {
          "User-Agent": "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36",
          plat: "h5"
        }
      }).promise.catch(() => {
        if (tryNum > 2) return Promise.reject(new Error("try max num"));
        return this.getListDetailMusicListByBDList(id, source, page, ++tryNum);
      });
      if (listData.code !== 200) return Promise.reject(new Error("failed"));
      return {
        list: this.filterBDListDetail(listData.data.list),
        page,
        limit: listData.data.pageSize,
        total: listData.data.total,
        source: "kw"
      };
    },
    async getListDetailMusicListByBD(id, page) {
      const uid = /uid=(\d+)/.exec(id)?.[1];
      const listId = /playlistId=(\d+)/.exec(id)?.[1];
      const source = /source=(\d+)/.exec(id)?.[1];
      if (!listId) return Promise.reject(new Error("failed"));
      const task = [this.getListDetailMusicListByBDList(listId, source, page)];
      switch (source) {
        case "4":
          task.push(this.getListDetailMusicListByBDListInfo(listId, source));
          break;
        case "5":
          task.push(this.getListDetailMusicListByBDUserPub(uid ?? listId));
          break;
      }
      const [listData, info] = await Promise.all(task);
      listData.info = info ?? {
        name: "",
        img: "",
        desc: "",
        author: "",
        play_count: ""
      };
      return listData;
    },
    // 获取歌曲列表内的音乐
    getListDetail(id, page, retryNum = 0) {
      if (/\/bodian\//.test(id)) return this.getListDetailMusicListByBD(id, page);
      if (/[?&:/]/.test(id)) id = id.replace(this.regExps.listDetailLink, "$1");
      else if (/^digest-/.test(id)) {
        let [digest, _id] = id.split("__");
        digest = digest.replace("digest-", "");
        id = _id;
        switch (digest) {
          case "8":
            break;
          case "13":
            return album_default.getAlbumListDetail(id, page, retryNum);
          case "5":
          default:
            return this.getListDetailDigest5(id, page, retryNum);
        }
      }
      return this.getListDetailDigest8(id, page, retryNum);
    },
    filterListDetail(rawData) {
      return rawData.map((item) => {
        let infoArr = item.N_MINFO.split(";");
        let types = [];
        let _types = {};
        for (let info of infoArr) {
          info = info.match(this.regExps.mInfo);
          if (info) {
            switch (info[2]) {
              case "4000":
                types.push({ type: "flac24bit", size: info[4] });
                _types.flac24bit = {
                  size: info[4].toLocaleUpperCase()
                };
                break;
              case "2000":
                types.push({ type: "flac", size: info[4] });
                _types.flac = {
                  size: info[4].toLocaleUpperCase()
                };
                break;
              case "320":
                types.push({ type: "320k", size: info[4] });
                _types["320k"] = {
                  size: info[4].toLocaleUpperCase()
                };
                break;
              case "128":
                types.push({ type: "128k", size: info[4] });
                _types["128k"] = {
                  size: info[4].toLocaleUpperCase()
                };
                break;
            }
          }
        }
        types.reverse();
        return {
          singer: formatSinger(decodeName(item.artist)),
          name: decodeName(item.name),
          albumName: decodeName(item.album),
          albumId: item.albumid,
          songmid: item.id,
          source: "kw",
          interval: formatPlayTime(parseInt(item.duration)),
          img: null,
          lrc: null,
          otherSource: null,
          types,
          _types,
          typeUrl: {}
        };
      });
    },
    getTags() {
      return Promise.all([this.getTag(), this.getHotTag()]).then(([tags, hotTag]) => ({ tags, hotTag, source: "kw" }));
    },
    getDetailPageUrl(id) {
      if (/[?&:/]/.test(id)) id = id.replace(this.regExps.listDetailLink, "$1");
      else if (/^digest-/.test(id)) {
        let result = id.split("__");
        id = result[1];
      }
      return `http://www.kuwo.cn/playlist_detail/${id}`;
    },
    search(text, page, limit = 20) {
      return httpFetch(`http://search.kuwo.cn/r.s?all=${encodeURIComponent(text)}&pn=${page - 1}&rn=${limit}&rformat=json&encoding=utf8&ver=mbox&vipver=MUSIC_8.7.7.0_BCS37&plat=pc&devid=28156413&ft=playlist&pay=0&needliveshow=0`).promise.then(({ body }) => {
        body = objStr2JSON(body);
        return {
          list: body.abslist.map((item) => {
            return {
              play_count: this.formatPlayCount(item.playcnt),
              id: item.playlistid,
              author: decodeName(item.nickname),
              name: decodeName(item.name),
              total: item.songnum,
              // time: item.publish_time,
              img: item.pic,
              desc: decodeName(item.intro),
              source: "kw"
            };
          }),
          limit,
          total: parseInt(body.TOTAL),
          source: "kw"
        };
      });
    }
  };

  // src/utils/musicSdk/kw/hotSearch.js
  var hotSearch_default = {
    _requestObj: null,
    async getList(retryNum = 0) {
      if (this._requestObj) this._requestObj.cancelHttp();
      if (retryNum > 2) return Promise.reject(new Error("try max num"));
      const _requestObj = httpFetch("http://hotword.kuwo.cn/hotword.s?prod=kwplayer_ar_9.3.0.1&corp=kuwo&newver=2&vipver=9.3.0.1&source=kwplayer_ar_9.3.0.1_40.apk&p2p=1&notrace=0&uid=0&plat=kwplayer_ar&rformat=json&encoding=utf8&tabid=1", {
        headers: {
          "User-Agent": "Dalvik/2.1.0 (Linux; U; Android 9;)"
        }
      });
      const { body, statusCode } = await _requestObj.promise;
      if (statusCode != 200 || body.status !== "ok") throw new Error("\u83B7\u53D6\u70ED\u641C\u8BCD\u5931\u8D25");
      return { source: "kw", list: this.filterList(body.tagvalue) };
    },
    filterList(rawList) {
      return rawList.map((item) => item.key);
    }
  };

  // src/utils/musicSdk/kw/comment.js
  var comment_default = {
    _requestObj: null,
    _requestObj2: null,
    async getComment({ songmid }, page = 1, limit = 20) {
      if (this._requestObj) this._requestObj.cancelHttp();
      const _requestObj = httpFetch(`http://ncomment.kuwo.cn/com.s?f=web&type=get_comment&aapiver=1&prod=kwplayer_ar_10.5.2.0&digest=15&sid=${songmid}&start=${limit * (page - 1)}&msgflag=1&count=${limit}&newver=3&uid=0`, {
        headers: {
          "User-Agent": "Dalvik/2.1.0 (Linux; U; Android 9;)"
        }
      });
      const { body, statusCode } = await _requestObj.promise;
      if (statusCode != 200 || body.code != "200") throw new Error("\u83B7\u53D6\u8BC4\u8BBA\u5931\u8D25");
      const total = body.comments_counts;
      return {
        source: "kw",
        comments: this.filterComment(body.comments),
        total,
        page,
        limit,
        maxPage: Math.ceil(total / limit) || 1
      };
    },
    async getHotComment({ songmid }, page = 1, limit = 100) {
      if (this._requestObj2) this._requestObj2.cancelHttp();
      const _requestObj2 = httpFetch(`http://ncomment.kuwo.cn/com.s?f=web&type=get_rec_comment&aapiver=1&prod=kwplayer_ar_10.5.2.0&digest=15&sid=${songmid}&start=${limit * (page - 1)}&msgflag=1&count=${limit}&newver=3&uid=0`, {
        headers: {
          "User-Agent": "Dalvik/2.1.0 (Linux; U; Android 9;)"
        }
      });
      const { body, statusCode } = await _requestObj2.promise;
      if (statusCode != 200 || body.code != "200") throw new Error("\u83B7\u53D6\u70ED\u95E8\u8BC4\u8BBA\u5931\u8D25");
      const total = body.hot_comments_counts;
      return {
        source: "kw",
        comments: this.filterComment(body.hot_comments),
        total,
        page,
        limit,
        maxPage: Math.ceil(total / limit) || 1
      };
    },
    filterComment(rawList) {
      if (!rawList) return [];
      return rawList.map((item) => {
        return {
          id: item.id,
          text: item.msg,
          time: item.time,
          timeStr: dateFormat2(Number(item.time) * 1e3),
          userName: item.u_name,
          avatar: item.u_pic,
          userId: item.u_id,
          likedCount: item.like_num,
          images: item.mpic ? [decodeURIComponent(item.mpic)] : [],
          reply: item.child_comments ? item.child_comments.map((i) => {
            return {
              id: i.id,
              text: i.msg,
              time: i.time,
              timeStr: dateFormat2(Number(i.time) * 1e3),
              userName: i.u_name,
              avatar: i.u_pic,
              userId: i.u_id,
              likedCount: i.like_num,
              images: i.mpic ? [i.mpic] : []
            };
          }) : []
        };
      });
    }
  };

  // src/utils/musicSdk/kw/index.js
  var kw = {
    _musicInfoRequestObj: null,
    _musicInfoPromiseCancelFn: null,
    _musicPicRequestObj: null,
    _musicPicPromiseCancelFn: null,
    // context: null,
    // init(context) {
    //   if (this.isInited) return
    //   this.isInited = true
    //   this.context = context
    //   // this.musicSearch.search('我又想你了').then(res => {
    //   //   console.log(res)
    //   // })
    //   // this.getMusicUrl('62355680', '320k').then(url => {
    //   //   console.log(url)
    //   // })
    // },
    tipSearch: tipSearch_default,
    musicSearch: musicSearch_default,
    leaderboard: leaderboard_default,
    songList: songList_default,
    hotSearch: hotSearch_default,
    comment: comment_default,
    getLyric(songInfo, isGetLyricx) {
      return lyric_default.getLyric(songInfo, isGetLyricx);
    },
    handleMusicInfo(songInfo) {
      return this.getMusicInfo(songInfo).then((info) => {
        songInfo.name = info.name;
        songInfo.singer = formatSinger(info.artist);
        songInfo.img = info.pic;
        songInfo.albumName = info.album;
        return songInfo;
      });
    },
    getMusicUrl(songInfo, type) {
      return apis("kw").getMusicUrl(songInfo, type);
    },
    getMusicInfo(songInfo) {
      if (this._musicInfoRequestObj) this._musicInfoRequestObj.cancelHttp();
      this._musicInfoRequestObj = httpFetch(`http://www.kuwo.cn/api/www/music/musicInfo?mid=${songInfo.songmid}`);
      return this._musicInfoRequestObj.promise.then(({ body }) => {
        return body.code === 200 ? body.data : Promise.reject(new Error(body.msg));
      });
    },
    getMusicUrls(musicInfo, cb) {
      let tasks = [];
      let songId = musicInfo.songmid;
      musicInfo.types.forEach((type) => {
        tasks.push(kw.getMusicUrl(songId, type.type).promise);
      });
      Promise.all(tasks).then((urlInfo) => {
        let typeUrl = {};
        urlInfo.forEach((info) => {
          typeUrl[info.type] = info.url;
        });
        cb(typeUrl);
      });
    },
    getPic(songInfo) {
      return pic_default.getPic(songInfo);
    },
    getMusicDetailPageUrl(songInfo) {
      return `http://www.kuwo.cn/play_detail/${songInfo.songmid}`;
    }
    // init() {
    //   return getToken()
    // },
  };
  var kw_default = kw;

  // src/utils/musicSdk/kg/leaderboard.js
  var boardList2 = [{ id: "kg__8888", name: "TOP500", bangid: "8888" }, { id: "kg__6666", name: "\u98D9\u5347\u699C", bangid: "6666" }, { id: "kg__59703", name: "\u8702\u9E1F\u6D41\u884C\u97F3\u4E50\u699C", bangid: "59703" }, { id: "kg__52144", name: "\u6296\u97F3\u70ED\u6B4C\u699C", bangid: "52144" }, { id: "kg__52767", name: "\u5FEB\u624B\u70ED\u6B4C\u699C", bangid: "52767" }, { id: "kg__24971", name: "DJ\u70ED\u6B4C\u699C", bangid: "24971" }, { id: "kg__23784", name: "\u7F51\u7EDC\u7EA2\u6B4C\u699C", bangid: "23784" }, { id: "kg__44412", name: "\u8BF4\u5531\u5148\u950B\u699C", bangid: "44412" }, { id: "kg__31308", name: "\u5185\u5730\u699C", bangid: "31308" }, { id: "kg__33160", name: "\u7535\u97F3\u699C", bangid: "33160" }, { id: "kg__31313", name: "\u9999\u6E2F\u5730\u533A\u699C", bangid: "31313" }, { id: "kg__51341", name: "\u6C11\u8C23\u699C", bangid: "51341" }, { id: "kg__54848", name: "\u53F0\u6E7E\u5730\u533A\u699C", bangid: "54848" }, { id: "kg__31310", name: "\u6B27\u7F8E\u699C", bangid: "31310" }, { id: "kg__33162", name: "ACG\u65B0\u6B4C\u699C", bangid: "33162" }, { id: "kg__31311", name: "\u97E9\u56FD\u699C", bangid: "31311" }, { id: "kg__31312", name: "\u65E5\u672C\u699C", bangid: "31312" }, { id: "kg__49225", name: "80\u540E\u70ED\u6B4C\u699C", bangid: "49225" }, { id: "kg__49223", name: "90\u540E\u70ED\u6B4C\u699C", bangid: "49223" }, { id: "kg__49224", name: "00\u540E\u70ED\u6B4C\u699C", bangid: "49224" }, { id: "kg__33165", name: "\u7CA4\u8BED\u91D1\u66F2\u699C", bangid: "33165" }, { id: "kg__33166", name: "\u6B27\u7F8E\u91D1\u66F2\u699C", bangid: "33166" }, { id: "kg__33163", name: "\u5F71\u89C6\u91D1\u66F2\u699C", bangid: "33163" }, { id: "kg__51340", name: "\u4F24\u611F\u699C", bangid: "51340" }, { id: "kg__35811", name: "\u4F1A\u5458\u4E13\u4EAB\u699C", bangid: "35811" }, { id: "kg__37361", name: "\u96F7\u8FBE\u699C", bangid: "37361" }, { id: "kg__21101", name: "\u5206\u4EAB\u699C", bangid: "21101" }, { id: "kg__46910", name: "\u7EFC\u827A\u65B0\u6B4C\u699C", bangid: "46910" }, { id: "kg__30972", name: "\u9177\u72D7\u97F3\u4E50\u4EBA\u539F\u521B\u699C", bangid: "30972" }, { id: "kg__60170", name: "\u95FD\u5357\u8BED\u699C", bangid: "60170" }, { id: "kg__65234", name: "\u513F\u6B4C\u699C", bangid: "65234" }, { id: "kg__4681", name: "\u7F8E\u56FDBillBoard\u699C", bangid: "4681" }, { id: "kg__25028", name: "Beatport\u7535\u5B50\u821E\u66F2\u699C", bangid: "25028" }, { id: "kg__4680", name: "\u82F1\u56FD\u5355\u66F2\u699C", bangid: "4680" }, { id: "kg__38623", name: "\u97E9\u56FDMelon\u97F3\u4E50\u699C", bangid: "38623" }, { id: "kg__42807", name: "joox\u672C\u5730\u70ED\u6B4C\u699C", bangid: "42807" }, { id: "kg__36107", name: "\u5C0F\u8BED\u79CD\u70ED\u6B4C\u699C", bangid: "36107" }, { id: "kg__4673", name: "\u65E5\u672C\u516C\u4FE1\u699C", bangid: "4673" }, { id: "kg__46868", name: "\u65E5\u672CSPACE SHOWER\u699C", bangid: "46868" }, { id: "kg__42808", name: "KKBOX\u98CE\u4E91\u699C", bangid: "42808" }, { id: "kg__60171", name: "\u8D8A\u5357\u8BED\u699C", bangid: "60171" }, { id: "kg__60172", name: "\u6CF0\u8BED\u699C", bangid: "60172" }, { id: "kg__59895", name: "R&B\u699C", bangid: "59895" }, { id: "kg__59896", name: "\u6447\u6EDA\u699C", bangid: "59896" }, { id: "kg__59897", name: "\u7235\u58EB\u699C", bangid: "59897" }, { id: "kg__59898", name: "\u4E61\u6751\u97F3\u4E50\u699C", bangid: "59898" }, { id: "kg__59900", name: "\u7EAF\u97F3\u4E50\u699C", bangid: "59900" }, { id: "kg__59899", name: "\u53E4\u5178\u699C", bangid: "59899" }, { id: "kg__22603", name: "5sing\u97F3\u4E50\u699C", bangid: "22603" }, { id: "kg__21335", name: "\u7E41\u661F\u97F3\u4E50\u699C", bangid: "21335" }, { id: "kg__33161", name: "\u53E4\u98CE\u65B0\u6B4C\u699C", bangid: "33161" }];
  var leaderboard_default2 = {
    listDetailLimit: 100,
    list: [
      {
        id: "kgtop500",
        name: "TOP500",
        bangid: "8888"
      },
      {
        id: "kgwlhgb",
        name: "\u7F51\u7EDC\u699C",
        bangid: "23784"
      },
      {
        id: "kgbsb",
        name: "\u98D9\u5347\u699C",
        bangid: "6666"
      },
      {
        id: "kgfxb",
        name: "\u5206\u4EAB\u699C",
        bangid: "21101"
      },
      {
        id: "kgcyyb",
        name: "\u7EAF\u97F3\u4E50\u699C",
        bangid: "33164"
      },
      {
        id: "kggfjqb",
        name: "\u53E4\u98CE\u699C",
        bangid: "33161"
      },
      {
        id: "kgyyjqb",
        name: "\u7CA4\u8BED\u699C",
        bangid: "33165"
      },
      {
        id: "kgomjqb",
        name: "\u6B27\u7F8E\u699C",
        bangid: "33166"
      },
      {
        id: "kgdyrgb",
        name: "\u7535\u97F3\u699C",
        bangid: "33160"
      },
      {
        id: "kgjdrgb",
        name: "DJ\u70ED\u6B4C\u699C",
        bangid: "24971"
      },
      {
        id: "kghyxgb",
        name: "\u534E\u8BED\u65B0\u6B4C\u699C",
        bangid: "31308"
      }
    ],
    getUrl(p, id, limit) {
      return `http://mobilecdnbj.kugou.com/api/v3/rank/song?version=9108&ranktype=1&plat=0&pagesize=${limit}&area_code=1&page=${p}&rankid=${id}&with_res_tag=0&show_portrait_mv=1`;
    },
    regExps: {
      total: /total: '(\d+)',/,
      page: /page: '(\d+)',/,
      limit: /pagesize: '(\d+)',/,
      listData: /global\.features = (\[.+\]);/
    },
    _requestBoardsObj: null,
    getBoardsData() {
      if (this._requestBoardsObj) this._requestBoardsObj.cancelHttp();
      this._requestBoardsObj = httpFetch("http://mobilecdnbj.kugou.com/api/v5/rank/list?version=9108&plat=0&showtype=2&parentid=0&apiver=6&area_code=1&withsong=1");
      return this._requestBoardsObj.promise;
    },
    getData(url) {
      const requestDataObj = httpFetch(url);
      return requestDataObj.promise;
    },
    getSinger(singers) {
      let arr = [];
      singers.forEach((singer) => {
        arr.push(singer.author_name);
      });
      return arr.join("\u3001");
    },
    filterData(rawList) {
      return rawList.map((item) => {
        const types = [];
        const _types = {};
        if (item.filesize !== 0) {
          let size = sizeFormate(item.filesize);
          types.push({ type: "128k", size, hash: item.hash });
          _types["128k"] = {
            size,
            hash: item.hash
          };
        }
        if (item["320filesize"] !== 0) {
          let size = sizeFormate(item["320filesize"]);
          types.push({ type: "320k", size, hash: item["320hash"] });
          _types["320k"] = {
            size,
            hash: item["320hash"]
          };
        }
        if (item.sqfilesize !== 0) {
          let size = sizeFormate(item.sqfilesize);
          types.push({ type: "flac", size, hash: item.sqhash });
          _types.flac = {
            size,
            hash: item.sqhash
          };
        }
        if (item.filesize_high !== 0) {
          let size = sizeFormate(item.filesize_high);
          types.push({ type: "flac24bit", size, hash: item.hash_high });
          _types.flac24bit = {
            size,
            hash: item.hash_high
          };
        }
        return {
          singer: formatSingerName(item.authors, "author_name"),
          name: decodeName(item.songname),
          albumName: decodeName(item.remark),
          albumId: item.album_id,
          songmid: item.audio_id,
          source: "kg",
          interval: formatPlayTime(item.duration),
          img: null,
          lrc: null,
          hash: item.hash,
          otherSource: null,
          types,
          _types,
          typeUrl: {}
        };
      });
    },
    filterBoardsData(rawList) {
      let list = [];
      for (const board of rawList) {
        if (board.isvol != 1) continue;
        list.push({
          id: "kg__" + board.rankid,
          name: board.rankname,
          bangid: String(board.rankid)
        });
      }
      return list;
    },
    async getBoards(retryNum = 0) {
      this.list = boardList2;
      return {
        list: boardList2,
        source: "kg"
      };
    },
    async getList(bangid, page, retryNum = 0) {
      if (++retryNum > 3) throw new Error("try max num");
      const { body } = await this.getData(this.getUrl(page, bangid, this.listDetailLimit));
      if (body.errcode != 0) return this.getList(bangid, page, retryNum);
      let total = body.data.total;
      let limit = 100;
      let listData = this.filterData(body.data.info);
      return {
        total,
        list: listData,
        limit,
        page,
        source: "kg"
      };
    },
    getDetailPageUrl(id) {
      if (typeof id == "string") id = id.replace("kg__", "");
      return `https://www.kugou.com/yy/rank/home/1-${id}.html`;
    }
  };

  // src/utils/musicSdk/kg/songList.js
  var import_infSign = __toESM(require_infSign_min());

  // src/utils/musicSdk/kg/util.js
  init_pako();
  var import_buffer = __toESM(require_buffer());
  var enc_key = import_buffer.Buffer.from([64, 71, 97, 119, 94, 50, 116, 71, 81, 54, 49, 45, 206, 210, 110, 105], "binary");
  var decodeLyric2 = (str) => new Promise((resolve, reject) => {
    if (!str.length) return;
    const buf_str = import_buffer.Buffer.from(str, "base64").slice(4);
    for (let i = 0, len = buf_str.length; i < len; i++) {
      buf_str[i] = buf_str[i] ^ enc_key[i % 16];
    }
    const result = pako_default.inflate(buf_str, { to: "string" });
    resolve(result);
  });
  var signatureParams = (params, platform = "android", body = "") => {
    let keyparam = "OIlwieks28dk2k092lksi2UIkp";
    if (platform === "web") keyparam = "NVPh5oo715z5DIWAeQlhMDsWXXQV4hwt";
    let param_list = params.split("&");
    param_list.sort();
    let sign_params = `${keyparam}${param_list.join("")}${body}${keyparam}`;
    return toMD5(sign_params);
  };

  // src/utils/musicSdk/kg/songList.js
  var handleSignature = (id, page, limit) => new Promise((resolve, reject) => {
    (0, import_infSign.default)({ appid: 1058, type: 0, module: "playlist", page, pagesize: limit, specialid: id }, null, {
      useH5: true,
      isCDN: true,
      callback(i) {
        resolve(i.signature);
      }
    });
  });
  var songList_default2 = {
    _requestObj_tags: null,
    _requestObj_listInfo: null,
    _requestObj_list: null,
    _requestObj_listRecommend: null,
    listDetailLimit: 1e4,
    currentTagInfo: {
      id: void 0,
      info: void 0
    },
    sortList: [
      {
        name: "\u63A8\u8350",
        tid: "recommend",
        id: "5"
      },
      {
        name: "\u6700\u70ED",
        tid: "hot",
        id: "6"
      },
      {
        name: "\u6700\u65B0",
        tid: "new",
        id: "7"
      },
      {
        name: "\u70ED\u85CF",
        tid: "hot_collect",
        id: "3"
      },
      {
        name: "\u98D9\u5347",
        tid: "rise",
        id: "8"
      }
    ],
    cache: /* @__PURE__ */ new Map(),
    regExps: {
      listData: /global\.data = (\[.+\]);/,
      listInfo: /global = {[\s\S]+?name: "(.+)"[\s\S]+?pic: "(.+)"[\s\S]+?};/,
      // https://www.kugou.com/yy/special/single/1067062.html
      listDetailLink: /^.+\/(\d+)\.html(?:\?.*|&.*$|#.*$|$)/
    },
    // async getGlobalSpecialId(specialId) {
    //   return httpFetch(`http://mobilecdnbj.kugou.com/api/v5/special/info?specialid=${specialId}`, {
    //     headers: {
    //       'User-Agent': 'Mozilla/5.0 (Linux; Android 10; HLK-AL00) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.5112.102 Mobile Safari/537.36 EdgA/104.0.1293.70',
    //     },
    //   }).promise.then(({ body }) => {
    //     // console.log(body)
    //     if (!body.data.global_specialid) Promise.reject(new Error('Failed to get global collection id.'))
    //     return body.data.global_specialid
    //   })
    // },
    // async getListInfoBySpecialId(special_id, retry = 0) {
    //   if (++retry > 2) throw new Error('failed')
    //   return httpFetch(`https://m.kugou.com/plist/list/${special_id}/?json=true`, {
    //     headers: {
    //       'User-Agent': 'Mozilla/5.0 (Linux; Android 10; HLK-AL00) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.5112.102 Mobile Safari/537.36 EdgA/104.0.1293.70',
    //     },
    //     follow_max: 2,
    //   }).promise.then(({ body }) => {
    //     // console.log(body)
    //     if (!body.info.list) return this.getListInfoBySpecialId(special_id, retry)
    //     let listinfo = body.info.list
    //     return {
    //       listInfo: {
    //         name: listinfo.specialname,
    //         image: listinfo.imgurl.replace('{size}', '150'),
    //         intro: listinfo.intro,
    //         author: listinfo.nickname,
    //         playcount: listinfo.playcount,
    //         total: listinfo.songcount,
    //       },
    //       globalSpecialId: listinfo.global_specialid,
    //     }
    //   })
    // },
    // async getSongListDetailByGlobalSpecialId(id, page, limit = 100, retry = 0) {
    //   if (++retry > 2) throw new Error('failed')
    //   console.log(id)
    //   const params = `specialid=0&need_sort=1&module=CloudMusic&clientver=11409&pagesize=${limit}&global_collection_id=${id}&userid=0&page=${page}&type=1&area_code=1&appid=1005`
    //   return httpFetch(`http://pubsongscdn.tx.kugou.com/v2/get_other_list_file?${params}&signature=${signatureParams(params)}`).promise.then(({ body }) => {
    //     // console.log(body)
    //     if (body.data?.info == null) return this.getSongListDetailByGlobalSpecialId(id, page, limit, retry)
    //     return body.data.info
    //   })
    // },
    parseHtmlDesc(html) {
      const prefix = '<div class="pc_specail_text pc_singer_tab_content" id="specailIntroduceWrap">';
      let index = html.indexOf(prefix);
      if (index < 0) return null;
      const afterStr = html.substring(index + prefix.length);
      index = afterStr.indexOf("</div>");
      if (index < 0) return null;
      return decodeName(afterStr.substring(0, index));
    },
    async getListDetailBySpecialId(id, page, tryNum = 0) {
      if (tryNum > 2) throw new Error("try max num");
      const { body } = await httpFetch(this.getSongListDetailUrl(id)).promise;
      let listData = body.match(this.regExps.listData);
      let listInfo = body.match(this.regExps.listInfo);
      if (!listData) return this.getListDetailBySpecialId(id, page, ++tryNum);
      let list = await this.getMusicInfos(JSON.parse(listData[1]));
      let name;
      let pic;
      if (listInfo) {
        name = listInfo[1];
        pic = listInfo[2];
      }
      let desc = this.parseHtmlDesc(body);
      return {
        list,
        page: 1,
        limit: 1e4,
        total: list.length,
        source: "kg",
        info: {
          name,
          img: pic,
          desc
          // author: body.result.info.userinfo.username,
          // play_count: formatPlayCount(body.result.listen_num),
        }
      };
    },
    getInfoUrl(tagId) {
      return tagId ? `http://www2.kugou.kugou.com/yueku/v9/special/getSpecial?is_smarty=1&cdn=cdn&t=5&c=${tagId}` : "http://www2.kugou.kugou.com/yueku/v9/special/getSpecial?is_smarty=1&";
    },
    getSongListUrl(sortId, tagId, page) {
      if (tagId == null) tagId = "";
      return `http://www2.kugou.kugou.com/yueku/v9/special/getSpecial?is_ajax=1&cdn=cdn&t=${sortId}&c=${tagId}&p=${page}`;
    },
    getSongListDetailUrl(id) {
      return `http://www2.kugou.kugou.com/yueku/v9/special/single/${id}-5-9999.html`;
    },
    filterInfoHotTag(rawData) {
      const result = [];
      if (rawData.status !== 1) return result;
      for (const key of Object.keys(rawData.data)) {
        let tag = rawData.data[key];
        result.push({
          id: tag.special_id,
          name: tag.special_name,
          source: "kg"
        });
      }
      return result;
    },
    filterTagInfo(rawData) {
      const result = [];
      for (const name of Object.keys(rawData)) {
        result.push({
          name,
          list: rawData[name].data.map((tag) => ({
            parent_id: tag.parent_id,
            parent_name: tag.pname,
            id: tag.id,
            name: tag.name,
            source: "kg"
          }))
        });
      }
      return result;
    },
    getSongList(sortId, tagId, page, tryNum = 0) {
      if (this._requestObj_list) this._requestObj_list.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      this._requestObj_list = httpFetch(
        this.getSongListUrl(sortId, tagId, page)
      );
      return this._requestObj_list.promise.then(({ body }) => {
        if (!body || body.status !== 1) return this.getSongList(sortId, tagId, page, ++tryNum);
        return this.filterList(body.special_db);
      });
    },
    getSongListRecommend(tryNum = 0) {
      if (this._requestObj_listRecommend) this._requestObj_listRecommend.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      this._requestObj_listRecommend = httpFetch(
        "http://everydayrec.service.kugou.com/guess_special_recommend",
        {
          method: "post",
          headers: {
            "User-Agent": "KuGou2012-8275-web_browser_event_handler"
          },
          body: {
            appid: 1001,
            clienttime: 1566798337219,
            clientver: 8275,
            key: "f1f93580115bb106680d2375f8032d96",
            mid: "21511157a05844bd085308bc76ef3343",
            platform: "pc",
            userid: "262643156",
            return_min: 6,
            return_max: 15
          }
        }
      );
      return this._requestObj_listRecommend.promise.then(({ body }) => {
        if (body.status !== 1) return this.getSongListRecommend(++tryNum);
        return this.filterList(body.data.special_list);
      });
    },
    filterList(rawData) {
      return rawData.map((item) => ({
        play_count: item.total_play_count || formatPlayCount(item.play_count),
        id: "id_" + item.specialid,
        author: item.nickname,
        name: item.specialname,
        time: dateFormat(item.publish_time || item.publishtime, "Y-M-D"),
        img: item.img || item.imgurl,
        total: item.songcount,
        grade: item.grade,
        desc: item.intro,
        source: "kg"
      }));
    },
    async createHttp(url, options, retryNum = 0) {
      if (retryNum > 2) throw new Error("try max num");
      let result;
      options.cache = "default";
      try {
        result = await httpFetch(url, options).promise;
      } catch (err) {
        console.log(err);
        return this.createHttp(url, options, ++retryNum);
      }
      if (result.statusCode !== 200 || (result.body.error_code !== void 0 ? result.body.error_code : result.body.errcode !== void 0 ? result.body.errcode : result.body.err_code) !== 0) return this.createHttp(url, options, ++retryNum);
      if (result.body.data) return result.body.data;
      if (Array.isArray(result.body.info)) return result.body;
      return result.body.info;
    },
    createTask(hashs) {
      let data = {
        area_code: "1",
        show_privilege: 1,
        show_album_info: "1",
        is_publish: "",
        appid: 1005,
        clientver: 11451,
        mid: "1",
        dfid: "-",
        clienttime: Date.now(),
        key: "OIlwieks28dk2k092lksi2UIkp",
        fields: "album_info,author_name,audio_info,ori_audio_name,base,songname"
      };
      let list = hashs;
      let tasks = [];
      while (list.length) {
        tasks.push(Object.assign({ data: list.slice(0, 100) }, data));
        if (list.length < 100) break;
        list = list.slice(100);
      }
      let url = "http://gateway.kugou.com/v2/album_audio/audio";
      return tasks.map((task) => this.createHttp(url, {
        method: "POST",
        body: task,
        headers: {
          "KG-THash": "13a3164",
          "KG-RC": "1",
          "KG-Fake": "0",
          "KG-RF": "00869891",
          "User-Agent": "Android712-AndroidPhone-11451-376-0-FeeCacheUpdate-wifi",
          "x-router": "kmr.service.kugou.com"
        }
      }).then((data2) => data2.map((s) => s[0])));
    },
    async getMusicInfos(list) {
      return this.filterData2(
        await Promise.all(
          this.createTask(
            this.deDuplication(list).map((item) => ({ hash: item.hash }))
          )
        ).then(([...datas]) => datas.flat())
      );
    },
    async getUserListDetailByCode(id) {
      const songInfo = await this.createHttp("http://t.kugou.com/command/", {
        method: "POST",
        headers: {
          "KG-RC": 1,
          "KG-THash": "network_super_call.cpp:3676261689:379",
          "User-Agent": ""
        },
        body: { appid: 1001, clientver: 9020, mid: "21511157a05844bd085308bc76ef3343", clienttime: 640612895, key: "36164c4015e704673c588ee202b9ecb8", data: id }
      });
      let songList;
      let info = songInfo.info;
      switch (info.type) {
        case 2:
          if (!info.global_collection_id) return this.getListDetailBySpecialId(info.id);
          break;
        default:
          break;
      }
      if (info.global_collection_id) return this.getUserListDetail2(info.global_collection_id);
      if (info.userid != null) {
        songList = await this.createHttp("http://www2.kugou.kugou.com/apps/kucodeAndShare/app/", {
          method: "POST",
          headers: {
            "KG-RC": 1,
            "KG-THash": "network_super_call.cpp:3676261689:379",
            "User-Agent": ""
          },
          body: { appid: 1001, clientver: 9020, mid: "21511157a05844bd085308bc76ef3343", clienttime: 640612895, key: "36164c4015e704673c588ee202b9ecb8", data: { id: info.id, type: 3, userid: info.userid, collect_type: 0, page: 1, pagesize: info.count } }
        });
      }
      let list = await this.getMusicInfos(songList || songInfo.list);
      return {
        list,
        page: 1,
        limit: info.count,
        total: list.length,
        source: "kg",
        info: {
          name: info.name,
          img: info.img_size && info.img_size.replace("{size}", 240) || info.img,
          // desc: body.result.info.list_desc,
          author: info.username
          // play_count: formatPlayCount(info.count),
        }
      };
    },
    async getUserListDetail3(chain, page) {
      const songInfo = await this.createHttp(`http://m.kugou.com/schain/transfer?pagesize=${this.listDetailLimit}&chain=${chain}&su=1&page=${page}&n=0.7928855356604456`, {
        headers: {
          "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1"
        }
      });
      if (!songInfo.list) {
        if (songInfo.global_collection_id) {
          return this.getListDetailByGcidChain(songInfo.global_collection_id, chain).catch(() => this.getUserListDetail2(songInfo.global_collection_id));
        } else return this.getUserListDetail4(songInfo, chain, page).catch(() => this.getUserListDetail5(chain));
      }
      let list = await this.getMusicInfos(songInfo.list);
      return {
        list,
        page: 1,
        limit: this.listDetailLimit,
        total: list.length,
        source: "kg",
        info: {
          name: songInfo.info.name,
          img: songInfo.info.img,
          // desc: body.result.info.list_desc,
          author: songInfo.info.username
          // play_count: formatPlayCount(info.count),
        }
      };
    },
    deDuplication(datas) {
      let ids = /* @__PURE__ */ new Set();
      return datas.filter(({ hash }) => {
        if (ids.has(hash)) return false;
        ids.add(hash);
        return true;
      });
    },
    async decodeGcid(gcid) {
      const params = "dfid=-&appid=1005&mid=0&clientver=20109&clienttime=640612895&uuid=-";
      const body = {
        ret_info: 1,
        data: [
          {
            id: gcid,
            id_type: 2
          }
        ]
      };
      const result = await this.createHttp(`https://t.kugou.com/v1/songlist/batch_decode?${params}&signature=${signatureParams(params, "android", JSON.stringify(body))}`, {
        method: "POST",
        headers: {
          "User-Agent": "Mozilla/5.0 (Linux; Android 10; HUAWEI HMA-AL00) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.106 Mobile Safari/537.36",
          Referer: "https://m.kugou.com/"
        },
        body
      });
      return result.list[0].global_collection_id;
    },
    async getUserListDetailByLink({ info }, link) {
      let listInfo = info["0"];
      let total = listInfo.count;
      let tasks = [];
      let page = 0;
      while (total) {
        const limit = total > 90 ? 90 : total;
        total -= limit;
        page += 1;
        tasks.push(this.createHttp(link.replace(/pagesize=\d+/, "pagesize=" + limit).replace(/page=\d+/, "page=" + page), {
          headers: {
            "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1",
            Referer: link
          }
        }).then((data) => data.list.info));
      }
      let result = await Promise.all(tasks).then(([...datas]) => datas.flat());
      result = await this.getMusicInfos(result);
      return {
        list: result,
        page,
        limit: this.listDetailLimit,
        total: result.length,
        source: "kg",
        info: {
          name: listInfo.name,
          img: listInfo.pic && listInfo.pic.replace("{size}", 240),
          // desc: body.result.info.list_desc,
          author: listInfo.list_create_username
          // play_count: formatPlayCount(listInfo.count),
        }
      };
    },
    createGetListDetail2Task(id, total) {
      let tasks = [];
      let page = 0;
      while (total) {
        const limit = total > 300 ? 300 : total;
        total -= limit;
        page += 1;
        const params = "appid=1058&global_specialid=" + id + "&specialid=0&plat=0&version=8000&page=" + page + "&pagesize=" + limit + "&srcappid=2919&clientver=20000&clienttime=1586163263991&mid=1586163263991&uuid=1586163263991&dfid=-";
        tasks.push(this.createHttp(`https://mobiles.kugou.com/api/v5/special/song_v2?${params}&signature=${signatureParams(params, "web")}`, {
          headers: {
            mid: "1586163263991",
            Referer: "https://m3ws.kugou.com/share/index.php",
            "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",
            dfid: "-",
            clienttime: "1586163263991"
          }
        }).then((data) => data.info));
      }
      return Promise.all(tasks).then(([...datas]) => datas.flat());
    },
    async getListDetailByGcidChain(id, chain) {
      const params = "srcappid=2919&clientver=20000&clienttime=1789566751657&mid=1789566751657&uuid=1789566751657&dfid=-&listid=15&type=0&pagesize=50&global_collection_id=" + id + "&page=1&share_type=collect&appid=1058&chain=" + chain;
      const data = await this.createHttp(`https://m3ws.kugou.com/v2/zlist/list?${params}&signature=${signatureParams(params, "web")}`, {
        headers: {
          mid: "1586163263991",
          Referer: "https://m3ws.kugou.com/share/index.php",
          "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",
          dfid: "-",
          clienttime: "1586163263991"
        }
      });
      const info = data.info[0];
      if (!info) throw new Error("get list error");
      let tasks = [Promise.resolve(data.list.info)];
      let page = 1;
      let totalPage = Math.ceil(info.count / 50);
      while (page < totalPage) {
        const limit = 50;
        page += 1;
        const params2 = "srcappid=2919&clientver=20000&clienttime=1789566751657&mid=1789566751657&uuid=1789566751657&dfid=-&listid=15&type=0&pagesize=" + limit + "&global_collection_id=" + id + "&page=" + page + "&share_type=collect&appid=1058&chain=" + chain;
        tasks.push(this.createHttp(`https://m3ws.kugou.com/v2/zlist/list?${params2}&signature=${signatureParams(params2, "web")}`, {
          headers: {
            mid: "1586163263991",
            Referer: "https://m3ws.kugou.com/share/index.php",
            "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",
            dfid: "-",
            clienttime: "1586163263991"
          }
        }).then((data2) => data2.list.info));
      }
      const hashs = await Promise.all(tasks).then(([...datas]) => datas.flat());
      let list = await this.getMusicInfos(hashs);
      return {
        list,
        page: 1,
        limit: this.listDetailLimit,
        total: info.count,
        source: "kg",
        info: {
          name: info.name,
          img: info.pic && info.pic.replace("{size}", 240),
          desc: info.intro,
          author: info.list_create_username
          // play_count: formatPlayCount(info.playcount),
        }
      };
    },
    async getUserListDetail2(global_collection_id) {
      let id = global_collection_id;
      if (id.length > 1e3) throw new Error("get list error");
      const params = "appid=1058&specialid=0&global_specialid=" + id + "&format=jsonp&srcappid=2919&clientver=20000&clienttime=1586163242519&mid=1586163242519&uuid=1586163242519&dfid=-";
      let info = await this.createHttp(`https://mobiles.kugou.com/api/v5/special/info_v2?${params}&signature=${signatureParams(params, "web")}`, {
        headers: {
          mid: "1586163242519",
          Referer: "https://m3ws.kugou.com/share/index.php",
          "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",
          dfid: "-",
          clienttime: "1586163242519"
        }
      });
      const songInfo = await this.createGetListDetail2Task(id, info.songcount);
      let list = await this.getMusicInfos(songInfo);
      return {
        list,
        page: 1,
        limit: this.listDetailLimit,
        total: list.length,
        source: "kg",
        info: {
          name: info.specialname,
          img: info.imgurl && info.imgurl.replace("{size}", 240),
          desc: info.intro,
          author: info.nickname,
          play_count: formatPlayCount(info.playcount)
        }
      };
    },
    async getListInfoByChain(chain) {
      if (this.cache.has(chain)) return this.cache.get(chain);
      const { body } = await httpFetch(`https://m.kugou.com/share/?chain=${chain}&id=${chain}`, {
        headers: {
          "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1"
        }
      }).promise;
      let result = body.match(/var\sphpParam\s=\s({.+?});/);
      if (result) result = JSON.parse(result[1]);
      this.cache.set(chain, result);
      return result;
    },
    async getUserListDetailByPcChain(chain) {
      let key = `${chain}_pc_list`;
      if (this.cache.has(key)) return this.cache.get(key);
      const { body } = await httpFetch(`http://www.kugou.com/share/${chain}.html`, {
        headers: {
          "User-Agent": "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36"
        }
      }).promise;
      let result = body.match(/var\sdataFromSmarty\s=\s(\[.+?\])/);
      if (result) result = JSON.parse(result[1]);
      this.cache.set(chain, result);
      result = await this.getMusicInfos(result);
      return result;
    },
    async getUserListDetail4(songInfo, chain, page) {
      const limit = 100;
      const [listInfo, list] = await Promise.all([
        this.getListInfoByChain(chain),
        this.getUserListDetailById(songInfo.id, page, limit)
      ]);
      return {
        list: list || [],
        page,
        limit,
        total: list.length ?? 0,
        source: "kg",
        info: {
          name: listInfo.specialname,
          img: listInfo.imgurl && listInfo.imgurl.replace("{size}", 240),
          // desc: body.result.info.list_desc,
          author: listInfo.nickname
          // play_count: formatPlayCount(info.count),
        }
      };
    },
    async getUserListDetail5(chain) {
      const [listInfo, list] = await Promise.all([
        this.getListInfoByChain(chain),
        this.getUserListDetailByPcChain(chain)
      ]);
      return {
        list: list || [],
        page: 1,
        limit: this.listDetailLimit,
        total: list.length ?? 0,
        source: "kg",
        info: {
          name: listInfo.specialname,
          img: listInfo.imgurl && listInfo.imgurl.replace("{size}", 240),
          // desc: body.result.info.list_desc,
          author: listInfo.nickname
          // play_count: formatPlayCount(info.count),
        }
      };
    },
    async getUserListDetailById(id, page, limit) {
      const signature = await handleSignature(id, page, limit);
      let info = await this.createHttp(`https://pubsongscdn.kugou.com/v2/get_other_list_file?srcappid=2919&clientver=20000&appid=1058&type=0&module=playlist&page=${page}&pagesize=${limit}&specialid=${id}&signature=${signature}`, {
        headers: {
          Referer: "https://m3ws.kugou.com/share/index.php",
          "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",
          dfid: "-"
        }
      });
      let result = await this.getMusicInfos(info.info);
      return result;
    },
    async getUserListDetail(link, page, retryNum = 0) {
      if (retryNum > 3) return Promise.reject(new Error("link try max num"));
      if (link.includes("#")) link = link.replace(/#.*$/, "");
      if (link.includes("global_collection_id") && link.includes("chain")) {
        return this.getListDetailByGcidChain(link.replace(/^.*?global_collection_id=(\w+)(?:&.*$|#.*$|$)/, "$1"), link.replace(/^.*?chain=(\w+)(?:&.*$|#.*$|$)/, "$1"));
      }
      if (link.includes("chain=")) return this.getUserListDetail3(link.replace(/^.*?chain=(\w+)(?:&.*$|#.*$|$)/, "$1"), page);
      if (link.includes("gcid_")) {
        let gcid = link.match(/gcid_\w+/)?.[0];
        if (gcid) {
          const global_collection_id = await this.decodeGcid(gcid);
          if (global_collection_id) return this.getUserListDetail2(global_collection_id);
        }
      }
      if (link.includes(".html")) {
        if (link.includes("zlist.html")) {
          link = link.replace(/^(.*)zlist\.html/, "https://m3ws.kugou.com/zlist/list");
          if (link.includes("pagesize")) {
            link = link.replace("pagesize=30", "pagesize=" + this.listDetailLimit).replace("page=1", "page=" + page);
          } else {
            link += `&pagesize=${this.listDetailLimit}&page=${page}`;
          }
        } else if (!link.includes("song.html")) return this.getUserListDetail3(link.replace(/.+\/(\w+).html(?:\?.*|&.*$|#.*$|$)/, "$1"), page);
      }
      if (link.includes("global_collection_id")) return this.getUserListDetail2(link.replace(/^.*?global_collection_id=(\w+)(?:&.*$|#.*$|$)/, "$1"));
      const requestObj_listDetailLink = httpFetch(link, {
        headers: {
          "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1",
          Referer: link
        }
      });
      const { url: location, statusCode, body } = await requestObj_listDetailLink.promise;
      if (statusCode > 400) return this.getUserListDetail(link, page, ++retryNum);
      if (location.split("?")[0] != link.split("?")[0]) {
        if (location.includes("global_collection_id") && location.includes("chain")) {
          return this.getListDetailByGcidChain(location.replace(/^.*?global_collection_id=(\w+)(?:&.*$|#.*$|$)/, "$1"), location.replace(/^.*?chain=(\w+)(?:&.*$|#.*$|$)/, "$1"));
        }
        if (location.includes("chain=")) return this.getUserListDetail3(location.replace(/^.*?chain=(\w+)(?:&.*$|#.*$|$)/, "$1"), page);
        if (location.includes("gcid_")) {
          let gcid = location.match(/gcid_\w+/)?.[0];
          if (gcid) {
            const global_collection_id = await this.decodeGcid(gcid);
            if (global_collection_id) return this.getUserListDetail2(global_collection_id);
          }
        }
        if (location.includes(".html")) {
          if (location.includes("zlist.html")) {
            let link2 = location.replace(/^(.*)zlist\.html/, "https://m3ws.kugou.com/zlist/list");
            if (link2.includes("pagesize")) {
              link2 = link2.replace("pagesize=30", "pagesize=" + this.listDetailLimit).replace("page=1", "page=" + page);
            } else {
              link2 += `&pagesize=${this.listDetailLimit}&page=${page}`;
            }
            return this.getUserListDetail(link2, page, ++retryNum);
          } else return this.getUserListDetail3(location.replace(/.+\/(\w+).html(?:\?.*|&.*$|#.*$|$)/, "$1"), page);
        }
        if (location.includes("global_collection_id")) return this.getUserListDetail2(location.replace(/^.*?global_collection_id=(\w+)(?:&.*$|#.*$|$)/, "$1"));
      }
      if (typeof body == "string") return this.getUserListDetail2(body.replace(/^[\s\S]+?"global_collection_id":"(\w+)"[\s\S]+?$/, "$1"));
      if (body.errcode !== 0) return this.getUserListDetail(link, page, ++retryNum);
      return this.getUserListDetailByLink(body, link);
    },
    async getListDetail(id, page) {
      id = id.toString();
      if (id.includes("special/single/")) {
        id = id.replace(this.regExps.listDetailLink, "$1");
      } else if (/https?:/.test(id)) {
        return this.getUserListDetail(id.replace(/^.*?http/, "http"), page);
      } else if (/^\d+$/.test(id)) {
        return this.getUserListDetailByCode(id);
      } else if (id.startsWith("id_")) {
        id = id.replace("id_", "");
      }
      return this.getListDetailBySpecialId(id, page);
    },
    filterData(rawList) {
      return rawList.map((item) => {
        const types = [];
        const _types = {};
        if (item.filesize !== 0) {
          let size = sizeFormate(item.filesize);
          types.push({ type: "128k", size, hash: item.hash });
          _types["128k"] = {
            size,
            hash: item.hash
          };
        }
        if (item.filesize_320 !== 0) {
          let size = sizeFormate(item.filesize_320);
          types.push({ type: "320k", size, hash: item.hash_320 });
          _types["320k"] = {
            size,
            hash: item.hash_320
          };
        }
        if (item.filesize_ape !== 0) {
          let size = sizeFormate(item.filesize_ape);
          types.push({ type: "ape", size, hash: item.hash_ape });
          _types.ape = {
            size,
            hash: item.hash_ape
          };
        }
        if (item.filesize_flac !== 0) {
          let size = sizeFormate(item.filesize_flac);
          types.push({ type: "flac", size, hash: item.hash_flac });
          _types.flac = {
            size,
            hash: item.hash_flac
          };
        }
        return {
          singer: decodeName(item.singername),
          name: decodeName(item.songname),
          albumName: decodeName(item.album_name),
          albumId: item.album_id,
          songmid: item.audio_id,
          source: "kg",
          interval: formatPlayTime(item.duration / 1e3),
          img: null,
          lrc: null,
          hash: item.hash,
          types,
          _types,
          typeUrl: {}
        };
      });
    },
    // getSinger(singers) {
    //   let arr = []
    //   singers?.forEach(singer => {
    //     arr.push(singer.name)
    //   })
    //   return arr.join('、')
    // },
    // v9 API
    // filterDatav9(rawList) {
    //   console.log(rawList)
    //   return rawList.map(item => {
    //     const types = []
    //     const _types = {}
    //     item.relate_goods.forEach(qualityObj => {
    //       if (qualityObj.level === 2) {
    //         let size = sizeFormate(qualityObj.size)
    //         types.push({ type: '128k', size, hash: qualityObj.hash })
    //         _types['128k'] = {
    //           size,
    //           hash: qualityObj.hash,
    //         }
    //       } else if (qualityObj.level === 4) {
    //         let size = sizeFormate(qualityObj.size)
    //         types.push({ type: '320k', size, hash: qualityObj.hash })
    //         _types['320k'] = {
    //           size,
    //           hash: qualityObj.hash,
    //         }
    //       } else if (qualityObj.level === 5) {
    //         let size = sizeFormate(qualityObj.size)
    //         types.push({ type: 'flac', size, hash: qualityObj.hash })
    //         _types.flac = {
    //           size,
    //           hash: qualityObj.hash,
    //         }
    //       } else if (qualityObj.level === 6) {
    //         let size = sizeFormate(qualityObj.size)
    //         types.push({ type: 'flac24bit', size, hash: qualityObj.hash })
    //         _types.flac24bit = {
    //           size,
    //           hash: qualityObj.hash,
    //         }
    //       }
    //     })
    //     const nameInfo = item.name.split(' - ')
    //     return {
    //       singer: this.getSinger(item.singerinfo),
    //       name: decodeName((nameInfo[1] ?? nameInfo[0]).trim()),
    //       albumName: decodeName(item.albuminfo.name),
    //       albumId: item.albuminfo.id,
    //       songmid: item.audio_id,
    //       source: 'kg',
    //       interval: formatPlayTime(item.timelen / 1000),
    //       img: null,
    //       lrc: null,
    //       hash: item.hash,
    //       types,
    //       _types,
    //       typeUrl: {},
    //     }
    //   })
    // },
    // hash list filter
    filterData2(rawList) {
      let ids = /* @__PURE__ */ new Set();
      let list = [];
      rawList.forEach((item) => {
        if (!item) return;
        if (ids.has(item.audio_info.audio_id)) return;
        ids.add(item.audio_info.audio_id);
        const types = [];
        const _types = {};
        if (item.audio_info.filesize !== "0") {
          let size = sizeFormate(parseInt(item.audio_info.filesize));
          types.push({ type: "128k", size, hash: item.audio_info.hash });
          _types["128k"] = {
            size,
            hash: item.audio_info.hash
          };
        }
        if (item.audio_info.filesize_320 !== "0") {
          let size = sizeFormate(parseInt(item.audio_info.filesize_320));
          types.push({ type: "320k", size, hash: item.audio_info.hash_320 });
          _types["320k"] = {
            size,
            hash: item.audio_info.hash_320
          };
        }
        if (item.audio_info.filesize_flac !== "0") {
          let size = sizeFormate(parseInt(item.audio_info.filesize_flac));
          types.push({ type: "flac", size, hash: item.audio_info.hash_flac });
          _types.flac = {
            size,
            hash: item.audio_info.hash_flac
          };
        }
        if (item.audio_info.filesize_high !== "0") {
          let size = sizeFormate(parseInt(item.audio_info.filesize_high));
          types.push({ type: "flac24bit", size, hash: item.audio_info.hash_high });
          _types.flac24bit = {
            size,
            hash: item.audio_info.hash_high
          };
        }
        list.push({
          singer: decodeName(item.author_name),
          name: decodeName(item.songname),
          albumName: decodeName(item.album_info.album_name),
          albumId: item.album_info.album_id,
          songmid: item.audio_info.audio_id,
          source: "kg",
          interval: formatPlayTime(parseInt(item.audio_info.timelength) / 1e3),
          img: null,
          lrc: null,
          hash: item.audio_info.hash,
          otherSource: null,
          types,
          _types,
          typeUrl: {}
        });
      });
      return list;
    },
    // 获取列表信息
    getListInfo(tagId, tryNum = 0) {
      if (this._requestObj_listInfo) this._requestObj_listInfo.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      this._requestObj_listInfo = httpFetch(this.getInfoUrl(tagId));
      return this._requestObj_listInfo.promise.then(({ body }) => {
        if (body.status !== 1) return this.getListInfo(tagId, ++tryNum);
        return {
          limit: body.data.params.pagesize,
          page: body.data.params.p,
          total: body.data.params.total,
          source: "kg"
        };
      });
    },
    // 获取列表数据
    getList(sortId, tagId, page) {
      let tasks = [this.getSongList(sortId, tagId, page)];
      tasks.push(
        this.currentTagInfo.id === tagId ? Promise.resolve(this.currentTagInfo.info) : this.getListInfo(tagId).then((info) => {
          this.currentTagInfo.id = tagId;
          this.currentTagInfo.info = Object.assign({}, info);
          return info;
        })
      );
      if (!tagId && page === 1 && sortId === this.sortList[0].id) tasks.push(this.getSongListRecommend());
      return Promise.all(tasks).then(([list, info, recommendList]) => {
        if (recommendList) list.unshift(...recommendList);
        return {
          list,
          ...info
        };
      });
    },
    // 获取标签
    getTags(tryNum = 0) {
      if (this._requestObj_tags) this._requestObj_tags.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      this._requestObj_tags = httpFetch(this.getInfoUrl());
      return this._requestObj_tags.promise.then(({ body }) => {
        if (body.status !== 1) return this.getTags(++tryNum);
        return {
          hotTag: this.filterInfoHotTag(body.data.hotTag),
          tags: this.filterTagInfo(body.data.tagids),
          source: "kg"
        };
      });
    },
    getDetailPageUrl(id) {
      if (typeof id == "string") {
        if (/^https?:\/\//.test(id)) return id;
        id = id.replace("id_", "");
      }
      return `https://www.kugou.com/yy/special/single/${id}.html`;
    },
    search(text, page, limit = 20) {
      return httpFetch(`http://msearchretry.kugou.com/api/v3/search/special?keyword=${encodeURIComponent(text)}&page=${page}&pagesize=${limit}&showtype=10&filter=0&version=7910&sver=2`).promise.then(({ body }) => {
        if (body.errcode != 0) throw new Error("filed");
        return {
          list: body.data.info.map((item) => {
            return {
              play_count: formatPlayCount(item.playcount),
              id: "id_" + item.specialid,
              author: item.nickname,
              name: item.specialname,
              time: dateFormat(item.publishtime, "Y-M-D"),
              img: item.imgurl,
              grade: item.grade,
              desc: item.intro,
              total: item.songcount,
              source: "kg"
            };
          }),
          limit,
          total: body.data.total,
          source: "kg"
        };
      });
    }
  };

  // src/utils/musicSdk/kg/musicSearch.js
  var musicSearch_default2 = {
    limit: 30,
    total: 0,
    page: 0,
    allPage: 1,
    musicSearch(str, page, limit) {
      const searchRequest = httpFetch(`https://songsearch.kugou.com/song_search_v2?keyword=${encodeURIComponent(str)}&page=${page}&pagesize=${limit}&userid=0&clientver=&platform=WebFilter&filter=2&iscorrection=1&privilege_filter=0&area_code=1`);
      return searchRequest.promise.then(({ body }) => body);
    },
    filterData(rawData) {
      const types = [];
      const _types = {};
      if (rawData.FileSize !== 0) {
        let size = sizeFormate(rawData.FileSize);
        types.push({ type: "128k", size, hash: rawData.FileHash });
        _types["128k"] = {
          size,
          hash: rawData.FileHash
        };
      }
      if (rawData.HQFileSize !== 0) {
        let size = sizeFormate(rawData.HQFileSize);
        types.push({ type: "320k", size, hash: rawData.HQFileHash });
        _types["320k"] = {
          size,
          hash: rawData.HQFileHash
        };
      }
      if (rawData.SQFileSize !== 0) {
        let size = sizeFormate(rawData.SQFileSize);
        types.push({ type: "flac", size, hash: rawData.SQFileHash });
        _types.flac = {
          size,
          hash: rawData.SQFileHash
        };
      }
      if (rawData.ResFileSize !== 0) {
        let size = sizeFormate(rawData.ResFileSize);
        types.push({ type: "flac24bit", size, hash: rawData.ResFileHash });
        _types.flac24bit = {
          size,
          hash: rawData.ResFileHash
        };
      }
      return {
        singer: decodeName(formatSingerName(rawData.Singers, "name")),
        name: decodeName(`${rawData.OriSongName}${rawData.Suffix ? ` ${rawData.Suffix}` : ""}`),
        albumName: decodeName(rawData.AlbumName),
        albumId: rawData.AlbumID,
        songmid: rawData.Audioid,
        source: "kg",
        interval: formatPlayTime(rawData.Duration),
        _interval: rawData.Duration,
        img: null,
        lrc: null,
        otherSource: null,
        hash: rawData.FileHash,
        types,
        _types,
        typeUrl: {}
      };
    },
    handleResult(rawData) {
      let ids = /* @__PURE__ */ new Set();
      const list = [];
      rawData.forEach((item) => {
        const key = item.Audioid + item.FileHash;
        if (ids.has(key)) return;
        ids.add(key);
        list.push(this.filterData(item));
        for (const childItem of item.Grp) {
          const key2 = item.Audioid + item.FileHash;
          if (ids.has(key2)) continue;
          ids.add(key2);
          list.push(this.filterData(childItem));
        }
      });
      return list;
    },
    search(str, page = 1, limit, retryNum = 0) {
      if (++retryNum > 3) return Promise.reject(new Error("try max num"));
      if (limit == null) limit = this.limit;
      return this.musicSearch(str, page, limit).then((result) => {
        if (!result || result.error_code !== 0) return this.search(str, page, limit, retryNum);
        let list = this.handleResult(result.data.lists);
        if (list == null) return this.search(str, page, limit, retryNum);
        this.total = result.data.total;
        this.page = page;
        this.allPage = Math.ceil(this.total / limit);
        return Promise.resolve({
          list,
          allPage: this.allPage,
          limit,
          total: this.total,
          source: "kg"
        });
      });
    }
  };

  // src/utils/musicSdk/kg/pic.js
  var pic_default2 = {
    getPic(songInfo) {
      const requestObj = httpFetch(
        "http://media.store.kugou.com/v1/get_res_privilege",
        {
          method: "POST",
          headers: {
            "KG-RC": 1,
            "KG-THash": "expand_search_manager.cpp:852736169:451",
            "User-Agent": "KuGou2012-9020-ExpandSearchManager"
          },
          body: {
            appid: 1001,
            area_code: "1",
            behavior: "play",
            clientver: "9020",
            need_hash_offset: 1,
            relate: 1,
            resource: [
              {
                album_audio_id: songInfo.songmid.length == 32 ? songInfo.audioId.split("_")[0] : songInfo.songmid,
                album_id: songInfo.albumId,
                hash: songInfo.hash,
                id: 0,
                name: `${songInfo.singer} - ${songInfo.name}.mp3`,
                type: "audio"
              }
            ],
            token: "",
            userid: 2626431536,
            vip: 1
          }
        }
      );
      return requestObj.promise.then(({ body }) => {
        if (body.error_code !== 0) return Promise.reject(new Error("\u56FE\u7247\u83B7\u53D6\u5931\u8D25"));
        let info = body.data[0].info;
        const img = info.imgsize ? info.image.replace("{size}", info.imgsize[0]) : info.image;
        if (!img) return Promise.reject(new Error("Pic get failed"));
        return img;
      });
    }
  };

  // src/utils/musicSdk/kg/lyric.js
  var headExp = /^.*\[id:\$\w+\]\n/;
  var parseLyric = (str) => {
    str = str.replace(/\r/g, "");
    if (headExp.test(str)) str = str.replace(headExp, "");
    let trans = str.match(/\[language:([\w=\\/+]+)\]/);
    let lyric;
    let rlyric;
    let tlyric;
    if (trans) {
      str = str.replace(/\[language:[\w=\\/+]+\]\n/, "");
      let json = JSON.parse(Buffer.from(trans[1], "base64").toString());
      for (const item of json.content) {
        switch (item.type) {
          case 0:
            rlyric = item.lyricContent;
            break;
          case 1:
            tlyric = item.lyricContent;
            break;
        }
      }
    }
    let i = 0;
    let lxlyric = str.replace(/\[((\d+),\d+)\].*/g, (str2) => {
      let result = str2.match(/\[((\d+),\d+)\].*/);
      let time = parseInt(result[2]);
      let ms = (time % 1e3).toString().padStart(3, "0");
      time /= 1e3;
      let m = parseInt(time / 60).toString().padStart(2, "0");
      time %= 60;
      let s = parseInt(time).toString().padStart(2, "0");
      time = `${m}:${s}.${ms}`;
      if (rlyric) rlyric[i] = `[${time}]${rlyric[i]?.join("") ?? ""}`;
      if (tlyric) tlyric[i] = `[${time}]${tlyric[i]?.join("") ?? ""}`;
      i++;
      return str2.replace(result[1], time);
    });
    rlyric = rlyric ? rlyric.join("\n") : "";
    tlyric = tlyric ? tlyric.join("\n") : "";
    lxlyric = lxlyric.replace(/<(\d+,\d+),\d+>/g, "<$1>");
    lxlyric = decodeName(lxlyric);
    lyric = lxlyric.replace(/<\d+,\d+>/g, "");
    rlyric = decodeName(rlyric);
    tlyric = decodeName(tlyric);
    return {
      lyric,
      tlyric,
      rlyric,
      lxlyric
    };
  };
  var lyric_default2 = {
    getIntv(interval) {
      if (!interval) return 0;
      let intvArr = interval.split(":");
      let intv = 0;
      let unit = 1;
      while (intvArr.length) {
        intv += intvArr.pop() * unit;
        unit *= 60;
      }
      return parseInt(intv);
    },
    // getLyric(songInfo, tryNum = 0) {
    //   let requestObj = httpFetch(`http://m.kugou.com/app/i/krc.php?cmd=100&keyword=${encodeURIComponent(songInfo.name)}&hash=${songInfo.hash}&timelength=${songInfo._interval || this.getIntv(songInfo.interval)}&d=0.38664927426725626`, {
    //     headers: {
    //       'KG-RC': 1,
    //       'KG-THash': 'expand_search_manager.cpp:852736169:451',
    //       'User-Agent': 'KuGou2012-9020-ExpandSearchManager',
    //     },
    //   })
    //   requestObj.promise = requestObj.promise.then(({ body, statusCode }) => {
    //     if (statusCode !== 200) {
    //       if (tryNum > 5) return Promise.reject(new Error('歌词获取失败'))
    //       let tryRequestObj = this.getLyric(songInfo, ++tryNum)
    //       requestObj.cancelHttp = tryRequestObj.cancelHttp.bind(tryRequestObj)
    //       return tryRequestObj.promise
    //     }
    //     return {
    //       lyric: body,
    //       tlyric: '',
    //     }
    //   })
    //   return requestObj
    // },
    searchLyric(name, hash, time, tryNum = 0) {
      let requestObj = httpFetch(`http://lyrics.kugou.com/search?ver=1&man=yes&client=pc&keyword=${encodeURIComponent(name)}&hash=${hash}&timelength=${time}&lrctxt=1`, {
        headers: {
          "KG-RC": 1,
          "KG-THash": "expand_search_manager.cpp:852736169:451",
          "User-Agent": "KuGou2012-9020-ExpandSearchManager"
        }
      });
      requestObj.promise = requestObj.promise.then(({ body, statusCode }) => {
        if (statusCode !== 200) {
          if (tryNum > 5) return Promise.reject(new Error("\u6B4C\u8BCD\u83B7\u53D6\u5931\u8D25"));
          let tryRequestObj = this.searchLyric(name, hash, time, ++tryNum);
          requestObj.cancelHttp = tryRequestObj.cancelHttp.bind(tryRequestObj);
          return tryRequestObj.promise;
        }
        if (body.candidates.length) {
          let info = body.candidates[0];
          return { id: info.id, accessKey: info.accesskey, fmt: info.krctype == 1 && info.contenttype != 1 ? "krc" : "lrc" };
        }
        return null;
      });
      return requestObj;
    },
    getLyricDownload(id, accessKey, fmt, tryNum = 0) {
      let requestObj = httpFetch(`http://lyrics.kugou.com/download?ver=1&client=pc&id=${id}&accesskey=${accessKey}&fmt=${fmt}&charset=utf8`, {
        headers: {
          "KG-RC": 1,
          "KG-THash": "expand_search_manager.cpp:852736169:451",
          "User-Agent": "KuGou2012-9020-ExpandSearchManager"
        }
      });
      requestObj.promise = requestObj.promise.then(({ body, statusCode }) => {
        if (statusCode !== 200) {
          if (tryNum > 5) return Promise.reject(new Error("\u6B4C\u8BCD\u83B7\u53D6\u5931\u8D25"));
          let tryRequestObj = this.getLyric(id, accessKey, fmt, ++tryNum);
          requestObj.cancelHttp = tryRequestObj.cancelHttp.bind(tryRequestObj);
          return tryRequestObj.promise;
        }
        switch (body.fmt) {
          case "krc":
            return decodeLyric2(body.content).then((result) => parseLyric(result));
          case "lrc":
            return {
              lyric: Buffer.from(body.content, "base64").toString("utf-8"),
              tlyric: "",
              rlyric: "",
              lxlyric: ""
            };
          default:
            return Promise.reject(new Error(`\u672A\u77E5\u6B4C\u8BCD\u683C\u5F0F: ${body.fmt}`));
        }
      });
      return requestObj;
    },
    getLyric(songInfo, tryNum = 0) {
      let requestObj = this.searchLyric(songInfo.name, songInfo.hash, songInfo._interval || this.getIntv(songInfo.interval));
      requestObj.promise = requestObj.promise.then((result) => {
        if (!result) return Promise.reject(new Error("Get lyric failed"));
        let requestObj2 = this.getLyricDownload(result.id, result.accessKey, result.fmt);
        requestObj.cancelHttp = requestObj2.cancelHttp.bind(requestObj2);
        return requestObj2.promise;
      });
      return requestObj;
    }
  };

  // src/utils/musicSdk/kg/hotSearch.js
  var hotSearch_default2 = {
    _requestObj: null,
    async getList(retryNum = 0) {
      if (this._requestObj) this._requestObj.cancelHttp();
      if (retryNum > 2) return Promise.reject(new Error("try max num"));
      const _requestObj = httpFetch("http://gateway.kugou.com/api/v3/search/hot_tab?signature=ee44edb9d7155821412d220bcaf509dd&appid=1005&clientver=10026&plat=0", {
        method: "get",
        cache: null,
        headers: {
          dfid: "1ssiv93oVqMp27cirf2CvoF1",
          mid: "156798703528610303473757548878786007104",
          clienttime: 1584257267,
          "x-router": "msearch.kugou.com",
          "user-agent": "Android9-AndroidPhone-10020-130-0-searchrecommendprotocol-wifi",
          "kg-rc": 1
        }
      });
      const { body, statusCode } = await _requestObj.promise;
      if (statusCode != 200 || body.errcode !== 0) throw new Error("\u83B7\u53D6\u70ED\u641C\u8BCD\u5931\u8D25");
      return { source: "kg", list: this.filterList(body.data.list) };
    },
    filterList(rawList) {
      const list = [];
      rawList.forEach((item) => {
        item.keywords.map((k) => list.push(decodeName(k.keyword)));
      });
      return list;
    }
  };

  // src/utils/musicSdk/kg/comment.js
  var comment_default2 = {
    _requestObj: null,
    _requestObj2: null,
    async getComment({ hash }, page = 1, limit = 20) {
      if (this._requestObj) this._requestObj.cancelHttp();
      let timestamp = Date.now();
      const params = `dfid=0&mid=16249512204336365674023395779019&clienttime=${timestamp}&uuid=0&extdata=${hash}&appid=1005&code=fc4be23b4e972707f36b8a828a93ba8a&schash=${hash}&clientver=11409&p=${page}&clienttoken=&pagesize=${limit}&ver=10&kugouid=0`;
      const _requestObj = httpFetch(`http://m.comment.service.kugou.com/r/v1/rank/newest?${params}&signature=${signatureParams(params)}`, {
        cache: "default",
        headers: {
          "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36 Edg/107.0.1418.24"
        }
      });
      const { body, statusCode } = await _requestObj.promise;
      if (statusCode != 200 || body.err_code !== 0) throw new Error("\u83B7\u53D6\u8BC4\u8BBA\u5931\u8D25");
      const total = body.count ?? 0;
      return { source: "kg", comments: this.filterComment(body.list || []), total, page, limit, maxPage: Math.ceil(total / limit) || 1 };
    },
    async getHotComment({ hash }, page = 1, limit = 20) {
      if (this._requestObj2) this._requestObj2.cancelHttp();
      let timestamp = Date.now();
      const params = `dfid=0&mid=16249512204336365674023395779019&clienttime=${timestamp}&uuid=0&extdata=${hash}&appid=1005&code=fc4be23b4e972707f36b8a828a93ba8a&schash=${hash}&clientver=11409&p=${page}&clienttoken=&pagesize=${limit}&ver=10&kugouid=0`;
      const _requestObj2 = httpFetch(`http://m.comment.service.kugou.com/r/v1/rank/topliked?${params}&signature=${signatureParams(params)}`, {
        cache: "default",
        headers: {
          "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36 Edg/107.0.1418.24"
        }
      });
      const { body, statusCode } = await _requestObj2.promise;
      if (statusCode != 200 || body.err_code !== 0) throw new Error("\u83B7\u53D6\u70ED\u95E8\u8BC4\u8BBA\u5931\u8D25");
      const total = body.count ?? 0;
      return { source: "kg", comments: this.filterComment(body.list || []), total, page, limit, maxPage: Math.ceil(total / limit) || 1 };
    },
    async getReplyComment({ songmid, audioId }, replyId, page = 1, limit = 100) {
      if (this._requestObj2) this._requestObj2.cancelHttp();
      songmid = songmid.length == 32 ? audioId.split("_")[0] : songmid;
      const _requestObj2 = httpFetch(`http://comment.service.kugou.com/index.php?r=commentsv2/getReplyWithLike&code=fc4be23b4e972707f36b8a828a93ba8a&p=${page}&pagesize=${limit}&ver=1.01&clientver=8373&kugouid=687373022&need_show_image=1&appid=1001&childrenid=${songmid}&tid=${replyId}`, {
        headers: {
          "User-Agent": "Android712-AndroidPhone-8983-18-0-COMMENT-wifi"
        }
      });
      const { body, statusCode } = await _requestObj2.promise;
      if (statusCode != 200 || body.err_code !== 0) throw new Error("\u83B7\u53D6\u56DE\u590D\u8BC4\u8BBA\u5931\u8D25");
      return { source: "kg", comments: this.filterComment(body.list || []) };
    },
    replaceAt(raw, atList) {
      atList.forEach((atobj) => {
        raw = raw.replaceAll(`[at=${atobj.id}]`, `@${atobj.name} `);
      });
      return raw;
    },
    filterComment(rawList) {
      return rawList.map((item) => {
        let data = {
          id: item.id,
          text: decodeName((item.atlist ? this.replaceAt(item.content, item.atlist) : item.content) || ""),
          images: item.images ? item.images.map((i) => i.url) : [],
          location: item.location,
          time: item.addtime,
          timeStr: dateFormat2(new Date(item.addtime).getTime()),
          userName: item.user_name,
          avatar: item.user_pic,
          userId: item.user_id,
          likedCount: item.like.likenum,
          replyNum: item.reply_num,
          reply: []
        };
        return item.pcontent ? {
          id: item.id,
          text: decodeName(item.pcontent),
          time: null,
          userName: item.puser,
          avatar: null,
          userId: item.puser_id,
          likedCount: null,
          replyNum: null,
          reply: [data]
        } : data;
      });
    }
  };

  // src/utils/musicSdk/kg/index.js
  var kg = {
    // tipSearch,
    leaderboard: leaderboard_default2,
    songList: songList_default2,
    musicSearch: musicSearch_default2,
    hotSearch: hotSearch_default2,
    comment: comment_default2,
    getMusicUrl(songInfo, type) {
      return apis("kg").getMusicUrl(songInfo, type);
    },
    getLyric(songInfo) {
      return lyric_default2.getLyric(songInfo);
    },
    // getLyric(songInfo) {
    //   return apis('kg').getLyric(songInfo)
    // },
    getPic(songInfo) {
      return pic_default2.getPic(songInfo);
    },
    getMusicDetailPageUrl(songInfo) {
      return `https://www.kugou.com/song/#hash=${songInfo.hash}&album_id=${songInfo.albumId}`;
    }
    // getPic(songInfo) {
    //   return apis('kg').getPic(songInfo)
    // },
  };
  var kg_default = kg;

  // src/utils/musicSdk/tx/leaderboard.js
  var boardList3 = [{ id: "tx__4", name: "\u6D41\u884C\u6307\u6570\u699C", bangid: "4" }, { id: "tx__26", name: "\u70ED\u6B4C\u699C", bangid: "26" }, { id: "tx__27", name: "\u65B0\u6B4C\u699C", bangid: "27" }, { id: "tx__62", name: "\u98D9\u5347\u699C", bangid: "62" }, { id: "tx__58", name: "\u8BF4\u5531\u699C", bangid: "58" }, { id: "tx__57", name: "\u559C\u529B\u7535\u97F3\u699C", bangid: "57" }, { id: "tx__28", name: "\u7F51\u7EDC\u6B4C\u66F2\u699C", bangid: "28" }, { id: "tx__5", name: "\u5185\u5730\u699C", bangid: "5" }, { id: "tx__3", name: "\u6B27\u7F8E\u699C", bangid: "3" }, { id: "tx__59", name: "\u9999\u6E2F\u5730\u533A\u699C", bangid: "59" }, { id: "tx__16", name: "\u97E9\u56FD\u699C", bangid: "16" }, { id: "tx__60", name: "\u6296\u5FEB\u699C", bangid: "60" }, { id: "tx__29", name: "\u5F71\u89C6\u91D1\u66F2\u699C", bangid: "29" }, { id: "tx__17", name: "\u65E5\u672C\u699C", bangid: "17" }, { id: "tx__52", name: "\u817E\u8BAF\u97F3\u4E50\u4EBA\u539F\u521B\u699C", bangid: "52" }, { id: "tx__36", name: "K\u6B4C\u91D1\u66F2\u699C", bangid: "36" }, { id: "tx__61", name: "\u53F0\u6E7E\u5730\u533A\u699C", bangid: "61" }, { id: "tx__63", name: "DJ\u821E\u66F2\u699C", bangid: "63" }, { id: "tx__64", name: "\u7EFC\u827A\u65B0\u6B4C\u699C", bangid: "64" }, { id: "tx__65", name: "\u56FD\u98CE\u70ED\u6B4C\u699C", bangid: "65" }, { id: "tx__67", name: "\u542C\u6B4C\u8BC6\u66F2\u699C", bangid: "67" }, { id: "tx__72", name: "\u52A8\u6F2B\u97F3\u4E50\u699C", bangid: "72" }, { id: "tx__73", name: "\u6E38\u620F\u97F3\u4E50\u699C", bangid: "73" }, { id: "tx__75", name: "\u6709\u58F0\u699C", bangid: "75" }, { id: "tx__131", name: "\u6821\u56ED\u97F3\u4E50\u4EBA\u6392\u884C\u699C", bangid: "131" }];
  var leaderboard_default3 = {
    limit: 300,
    list: [
      {
        id: "txlxzsb",
        name: "\u6D41\u884C\u699C",
        bangid: 4
      },
      {
        id: "txrgb",
        name: "\u70ED\u6B4C\u699C",
        bangid: 26
      },
      {
        id: "txwlhgb",
        name: "\u7F51\u7EDC\u699C",
        bangid: 28
      },
      {
        id: "txdyb",
        name: "\u6296\u97F3\u699C",
        bangid: 60
      },
      {
        id: "txndb",
        name: "\u5185\u5730\u699C",
        bangid: 5
      },
      {
        id: "txxgb",
        name: "\u9999\u6E2F\u699C",
        bangid: 59
      },
      {
        id: "txtwb",
        name: "\u53F0\u6E7E\u699C",
        bangid: 61
      },
      {
        id: "txoumb",
        name: "\u6B27\u7F8E\u699C",
        bangid: 3
      },
      {
        id: "txhgb",
        name: "\u97E9\u56FD\u699C",
        bangid: 16
      },
      {
        id: "txrbb",
        name: "\u65E5\u672C\u699C",
        bangid: 17
      },
      {
        id: "txtybb",
        name: "YouTube\u699C",
        bangid: 128
      }
    ],
    listDetailRequest(id, period, limit) {
      return httpFetch("https://u.y.qq.com/cgi-bin/musicu.fcg", {
        method: "post",
        headers: {
          "User-Agent": "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)"
        },
        body: {
          toplist: {
            module: "musicToplist.ToplistInfoServer",
            method: "GetDetail",
            param: {
              topid: id,
              num: limit,
              period
            }
          },
          comm: {
            uin: 0,
            format: "json",
            ct: 20,
            cv: 1859
          }
        }
      }).promise;
    },
    regExps: {
      periodList: /<i class="play_cover__btn c_tx_link js_icon_play" data-listkey=".+?" data-listname=".+?" data-tid=".+?" data-date=".+?" .+?<\/i>/g,
      period: /data-listname="(.+?)" data-tid=".*?\/(.+?)" data-date="(.+?)" .+?<\/i>/
    },
    periods: {},
    periodUrl: "https://c.y.qq.com/node/pc/wk_v15/top.html",
    _requestBoardsObj: null,
    getBoardsData() {
      if (this._requestBoardsObj) this._requestBoardsObj.cancelHttp();
      this._requestBoardsObj = httpFetch("https://c.y.qq.com/v8/fcg-bin/fcg_myqq_toplist.fcg?g_tk=1928093487&inCharset=utf-8&outCharset=utf-8&notice=0&format=json&uin=0&needNewCode=1&platform=h5");
      return this._requestBoardsObj.promise;
    },
    getData(url) {
      const requestDataObj = httpFetch(url);
      return requestDataObj.promise;
    },
    filterData(rawList) {
      return rawList.map((item) => {
        let types = [];
        let _types = {};
        if (item.file.size_128mp3 !== 0) {
          let size = sizeFormate(item.file.size_128mp3);
          types.push({ type: "128k", size });
          _types["128k"] = {
            size
          };
        }
        if (item.file.size_320mp3 !== 0) {
          let size = sizeFormate(item.file.size_320mp3);
          types.push({ type: "320k", size });
          _types["320k"] = {
            size
          };
        }
        if (item.file.size_flac !== 0) {
          let size = sizeFormate(item.file.size_flac);
          types.push({ type: "flac", size });
          _types.flac = {
            size
          };
        }
        if (item.file.size_hires !== 0) {
          let size = sizeFormate(item.file.size_hires);
          types.push({ type: "flac24bit", size });
          _types.flac24bit = {
            size
          };
        }
        return {
          singer: formatSingerName(item.singer, "name"),
          name: item.title,
          albumName: item.album.name,
          albumId: item.album.mid,
          source: "tx",
          interval: formatPlayTime(item.interval),
          songId: item.id,
          albumMid: item.album.mid,
          strMediaMid: item.file.media_mid,
          songmid: item.mid,
          img: item.album.name === "" || item.album.name === "\u7A7A" ? item.singer?.length ? `https://y.gtimg.cn/music/photo_new/T001R500x500M000${item.singer[0].mid}.jpg` : "" : `https://y.gtimg.cn/music/photo_new/T002R500x500M000${item.album.mid}.jpg`,
          lrc: null,
          otherSource: null,
          types,
          _types,
          typeUrl: {}
        };
      });
    },
    getPeriods(bangid) {
      return this.getData(this.periodUrl).then(({ body: html }) => {
        let result = html.match(this.regExps.periodList);
        if (!result) return Promise.reject(new Error("get data failed"));
        result.forEach((item) => {
          let result2 = item.match(this.regExps.period);
          if (!result2) return;
          this.periods[result2[2]] = {
            name: result2[1],
            bangid: result2[2],
            period: result2[3]
          };
        });
        const info = this.periods[bangid];
        return info && info.period;
      });
    },
    filterBoardsData(rawList) {
      let list = [];
      for (const board of rawList) {
        if (board.id == 201) continue;
        if (board.topTitle.startsWith("\u5DC5\u5CF0\u699C\xB7")) {
          board.topTitle = board.topTitle.substring(4, board.topTitle.length);
        }
        if (!board.topTitle.endsWith("\u699C")) board.topTitle += "\u699C";
        list.push({
          id: "tx__" + board.id,
          name: board.topTitle,
          bangid: String(board.id)
        });
      }
      return list;
    },
    async getBoards(retryNum = 0) {
      this.list = boardList3;
      return {
        list: boardList3,
        source: "tx"
      };
    },
    getList(bangid, page, retryNum = 0) {
      if (++retryNum > 3) return Promise.reject(new Error("try max num"));
      bangid = parseInt(bangid);
      let info = this.periods[bangid];
      let p = info ? Promise.resolve(info.period) : this.getPeriods(bangid);
      return p.then((period) => {
        return this.listDetailRequest(bangid, period, this.limit).then((resp) => {
          if (resp.body.code !== 0) return this.getList(bangid, page, retryNum);
          return {
            total: resp.body.toplist.data.songInfoList.length,
            list: this.filterData(resp.body.toplist.data.songInfoList),
            limit: this.limit,
            page: 1,
            source: "tx"
          };
        });
      });
    },
    getDetailPageUrl(id) {
      if (typeof id == "string") id = id.replace("tx__", "");
      return `https://y.qq.com/n/ryqq/toplist/${id}`;
    }
  };

  // src/utils/musicSdk/tx/musicInfo.js
  var getSinger = (singers) => {
    let arr = [];
    singers.forEach((singer) => {
      arr.push(singer.name);
    });
    return arr.join("\u3001");
  };
  var musicInfo_default = (songmid) => {
    const requestObj = httpFetch("https://u.y.qq.com/cgi-bin/musicu.fcg", {
      method: "post",
      headers: {
        "User-Agent": "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)"
      },
      body: {
        comm: {
          ct: "19",
          cv: "1859",
          uin: "0"
        },
        req: {
          module: "music.pf_song_detail_svr",
          method: "get_song_detail_yqq",
          param: {
            song_type: 0,
            song_mid: songmid
          }
        }
      }
    });
    return requestObj.promise.then(({ body }) => {
      if (body.code != 0 || body.req.code != 0) return Promise.reject(new Error("\u83B7\u53D6\u6B4C\u66F2\u4FE1\u606F\u5931\u8D25"));
      const item = body.req.data.track_info;
      if (!item.file?.media_mid) return null;
      let types = [];
      let _types = {};
      const file = item.file;
      if (file.size_128mp3 != 0) {
        let size = sizeFormate(file.size_128mp3);
        types.push({ type: "128k", size });
        _types["128k"] = {
          size
        };
      }
      if (file.size_320mp3 !== 0) {
        let size = sizeFormate(file.size_320mp3);
        types.push({ type: "320k", size });
        _types["320k"] = {
          size
        };
      }
      if (file.size_flac !== 0) {
        let size = sizeFormate(file.size_flac);
        types.push({ type: "flac", size });
        _types.flac = {
          size
        };
      }
      if (file.size_hires !== 0) {
        let size = sizeFormate(file.size_hires);
        types.push({ type: "flac24bit", size });
        _types.flac24bit = {
          size
        };
      }
      let albumId = "";
      let albumName = "";
      if (item.album) {
        albumName = item.album.name;
        albumId = item.album.mid;
      }
      return {
        singer: getSinger(item.singer),
        name: item.title,
        albumName,
        albumId,
        source: "tx",
        interval: formatPlayTime(item.interval),
        songId: item.id,
        albumMid: item.album?.mid ?? "",
        strMediaMid: item.file.media_mid,
        songmid: item.mid,
        img: albumId === "" || albumId === "\u7A7A" ? item.singer?.length ? `https://y.gtimg.cn/music/photo_new/T001R500x500M000${item.singer[0].mid}.jpg` : "" : `https://y.gtimg.cn/music/photo_new/T002R500x500M000${albumId}.jpg`,
        types,
        _types,
        typeUrl: {}
      };
    });
  };

  // src/utils/musicSdk/tx/qrcDecode.js
  init_pako();
  var DES_ENCRYPT = 1;
  var DES_DECRYPT = 0;
  var sbox = [
    [
      14,
      4,
      13,
      1,
      2,
      15,
      11,
      8,
      3,
      10,
      6,
      12,
      5,
      9,
      0,
      7,
      0,
      15,
      7,
      4,
      14,
      2,
      13,
      1,
      10,
      6,
      12,
      11,
      9,
      5,
      3,
      8,
      4,
      1,
      14,
      8,
      13,
      6,
      2,
      11,
      15,
      12,
      9,
      7,
      3,
      10,
      5,
      0,
      15,
      12,
      8,
      2,
      4,
      9,
      1,
      7,
      5,
      11,
      3,
      14,
      10,
      0,
      6,
      13
    ],
    [
      15,
      1,
      8,
      14,
      6,
      11,
      3,
      4,
      9,
      7,
      2,
      13,
      12,
      0,
      5,
      10,
      3,
      13,
      4,
      7,
      15,
      2,
      8,
      15,
      12,
      0,
      1,
      10,
      6,
      9,
      11,
      5,
      0,
      14,
      7,
      11,
      10,
      4,
      13,
      1,
      5,
      8,
      12,
      6,
      9,
      3,
      2,
      15,
      13,
      8,
      10,
      1,
      3,
      15,
      4,
      2,
      11,
      6,
      7,
      12,
      0,
      5,
      14,
      9
    ],
    [
      10,
      0,
      9,
      14,
      6,
      3,
      15,
      5,
      1,
      13,
      12,
      7,
      11,
      4,
      2,
      8,
      13,
      7,
      0,
      9,
      3,
      4,
      6,
      10,
      2,
      8,
      5,
      14,
      12,
      11,
      15,
      1,
      13,
      6,
      4,
      9,
      8,
      15,
      3,
      0,
      11,
      1,
      2,
      12,
      5,
      10,
      14,
      7,
      1,
      10,
      13,
      0,
      6,
      9,
      8,
      7,
      4,
      15,
      14,
      3,
      11,
      5,
      2,
      12
    ],
    [
      7,
      13,
      14,
      3,
      0,
      6,
      9,
      10,
      1,
      2,
      8,
      5,
      11,
      12,
      4,
      15,
      13,
      8,
      11,
      5,
      6,
      15,
      0,
      3,
      4,
      7,
      2,
      12,
      1,
      10,
      14,
      9,
      10,
      6,
      9,
      0,
      12,
      11,
      7,
      13,
      15,
      1,
      3,
      14,
      5,
      2,
      8,
      4,
      3,
      15,
      0,
      6,
      10,
      10,
      13,
      8,
      9,
      4,
      5,
      11,
      12,
      7,
      2,
      14
    ],
    [
      2,
      12,
      4,
      1,
      7,
      10,
      11,
      6,
      8,
      5,
      3,
      15,
      13,
      0,
      14,
      9,
      14,
      11,
      2,
      12,
      4,
      7,
      13,
      1,
      5,
      0,
      15,
      10,
      3,
      9,
      8,
      6,
      4,
      2,
      1,
      11,
      10,
      13,
      7,
      8,
      15,
      9,
      12,
      5,
      6,
      3,
      0,
      14,
      11,
      8,
      12,
      7,
      1,
      14,
      2,
      13,
      6,
      15,
      0,
      9,
      10,
      4,
      5,
      3
    ],
    [
      12,
      1,
      10,
      15,
      9,
      2,
      6,
      8,
      0,
      13,
      3,
      4,
      14,
      7,
      5,
      11,
      10,
      15,
      4,
      2,
      7,
      12,
      9,
      5,
      6,
      1,
      13,
      14,
      0,
      11,
      3,
      8,
      9,
      14,
      15,
      5,
      2,
      8,
      12,
      3,
      7,
      0,
      4,
      10,
      1,
      13,
      11,
      6,
      4,
      3,
      2,
      12,
      9,
      5,
      15,
      10,
      11,
      14,
      1,
      7,
      6,
      0,
      8,
      13
    ],
    [
      4,
      11,
      2,
      14,
      15,
      0,
      8,
      13,
      3,
      12,
      9,
      7,
      5,
      10,
      6,
      1,
      13,
      0,
      11,
      7,
      4,
      9,
      1,
      10,
      14,
      3,
      5,
      12,
      2,
      15,
      8,
      6,
      1,
      4,
      11,
      13,
      12,
      3,
      7,
      14,
      10,
      15,
      6,
      8,
      0,
      5,
      9,
      2,
      6,
      11,
      13,
      8,
      1,
      4,
      10,
      7,
      9,
      5,
      0,
      15,
      14,
      2,
      3,
      12
    ],
    [
      13,
      2,
      8,
      4,
      6,
      15,
      11,
      1,
      10,
      9,
      3,
      14,
      5,
      0,
      12,
      7,
      1,
      15,
      13,
      8,
      10,
      3,
      7,
      4,
      12,
      5,
      6,
      11,
      0,
      14,
      9,
      2,
      7,
      11,
      4,
      1,
      9,
      12,
      14,
      2,
      0,
      6,
      10,
      13,
      15,
      3,
      5,
      8,
      2,
      1,
      14,
      7,
      4,
      10,
      8,
      13,
      15,
      12,
      9,
      0,
      3,
      5,
      6,
      11
    ]
  ];
  var key_rnd_shift = [1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1];
  var key_perm_c = [
    56,
    48,
    40,
    32,
    24,
    16,
    8,
    0,
    57,
    49,
    41,
    33,
    25,
    17,
    9,
    1,
    58,
    50,
    42,
    34,
    26,
    18,
    10,
    2,
    59,
    51,
    43,
    35
  ];
  var key_perm_d = [
    62,
    54,
    46,
    38,
    30,
    22,
    14,
    6,
    61,
    53,
    45,
    37,
    29,
    21,
    13,
    5,
    60,
    52,
    44,
    36,
    28,
    20,
    12,
    4,
    27,
    19,
    11,
    3
  ];
  var key_compression = [
    13,
    16,
    10,
    23,
    0,
    4,
    2,
    27,
    14,
    5,
    20,
    9,
    22,
    18,
    11,
    3,
    25,
    7,
    15,
    6,
    26,
    19,
    12,
    1,
    40,
    51,
    30,
    36,
    46,
    54,
    29,
    39,
    50,
    44,
    32,
    47,
    43,
    48,
    38,
    55,
    33,
    52,
    45,
    41,
    49,
    35,
    28,
    31
  ];
  var bitnum = (a, b, c) => (a[(b / 32 | 0) * 4 + 3 - (b % 32 / 8 | 0)] >>> 7 - b % 8 & 1) << c >>> 0;
  var bitnum_intr = (a, b, c) => (a >>> 31 - b & 1) << c >>> 0;
  var bitnum_intl = (a, b, c) => (a << b >>> 0 & 2147483648) >>> 0 >>> c >>> 0;
  var sbox_bit = (a) => a & 32 | (a & 31) >>> 1 | (a & 1) << 4;
  var initial_permutation = (input) => {
    const s0 = (bitnum(input, 57, 31) | bitnum(input, 49, 30) | bitnum(input, 41, 29) | bitnum(input, 33, 28) | bitnum(input, 25, 27) | bitnum(input, 17, 26) | bitnum(input, 9, 25) | bitnum(input, 1, 24) | bitnum(input, 59, 23) | bitnum(input, 51, 22) | bitnum(input, 43, 21) | bitnum(input, 35, 20) | bitnum(input, 27, 19) | bitnum(input, 19, 18) | bitnum(input, 11, 17) | bitnum(input, 3, 16) | bitnum(input, 61, 15) | bitnum(input, 53, 14) | bitnum(input, 45, 13) | bitnum(input, 37, 12) | bitnum(input, 29, 11) | bitnum(input, 21, 10) | bitnum(input, 13, 9) | bitnum(input, 5, 8) | bitnum(input, 63, 7) | bitnum(input, 55, 6) | bitnum(input, 47, 5) | bitnum(input, 39, 4) | bitnum(input, 31, 3) | bitnum(input, 23, 2) | bitnum(input, 15, 1) | bitnum(input, 7, 0)) >>> 0;
    const s1 = (bitnum(input, 56, 31) | bitnum(input, 48, 30) | bitnum(input, 40, 29) | bitnum(input, 32, 28) | bitnum(input, 24, 27) | bitnum(input, 16, 26) | bitnum(input, 8, 25) | bitnum(input, 0, 24) | bitnum(input, 58, 23) | bitnum(input, 50, 22) | bitnum(input, 42, 21) | bitnum(input, 34, 20) | bitnum(input, 26, 19) | bitnum(input, 18, 18) | bitnum(input, 10, 17) | bitnum(input, 2, 16) | bitnum(input, 60, 15) | bitnum(input, 52, 14) | bitnum(input, 44, 13) | bitnum(input, 36, 12) | bitnum(input, 28, 11) | bitnum(input, 20, 10) | bitnum(input, 12, 9) | bitnum(input, 4, 8) | bitnum(input, 62, 7) | bitnum(input, 54, 6) | bitnum(input, 46, 5) | bitnum(input, 38, 4) | bitnum(input, 30, 3) | bitnum(input, 22, 2) | bitnum(input, 14, 1) | bitnum(input, 6, 0)) >>> 0;
    return [s0, s1];
  };
  var inverse_permutation = (s0, s1, out) => {
    out[3] = (bitnum_intr(s1, 7, 7) | bitnum_intr(s0, 7, 6) | bitnum_intr(s1, 15, 5) | bitnum_intr(s0, 15, 4) | bitnum_intr(s1, 23, 3) | bitnum_intr(s0, 23, 2) | bitnum_intr(s1, 31, 1) | bitnum_intr(s0, 31, 0)) & 255;
    out[2] = (bitnum_intr(s1, 6, 7) | bitnum_intr(s0, 6, 6) | bitnum_intr(s1, 14, 5) | bitnum_intr(s0, 14, 4) | bitnum_intr(s1, 22, 3) | bitnum_intr(s0, 22, 2) | bitnum_intr(s1, 30, 1) | bitnum_intr(s0, 30, 0)) & 255;
    out[1] = (bitnum_intr(s1, 5, 7) | bitnum_intr(s0, 5, 6) | bitnum_intr(s1, 13, 5) | bitnum_intr(s0, 13, 4) | bitnum_intr(s1, 21, 3) | bitnum_intr(s0, 21, 2) | bitnum_intr(s1, 29, 1) | bitnum_intr(s0, 29, 0)) & 255;
    out[0] = (bitnum_intr(s1, 4, 7) | bitnum_intr(s0, 4, 6) | bitnum_intr(s1, 12, 5) | bitnum_intr(s0, 12, 4) | bitnum_intr(s1, 20, 3) | bitnum_intr(s0, 20, 2) | bitnum_intr(s1, 28, 1) | bitnum_intr(s0, 28, 0)) & 255;
    out[7] = (bitnum_intr(s1, 3, 7) | bitnum_intr(s0, 3, 6) | bitnum_intr(s1, 11, 5) | bitnum_intr(s0, 11, 4) | bitnum_intr(s1, 19, 3) | bitnum_intr(s0, 19, 2) | bitnum_intr(s1, 27, 1) | bitnum_intr(s0, 27, 0)) & 255;
    out[6] = (bitnum_intr(s1, 2, 7) | bitnum_intr(s0, 2, 6) | bitnum_intr(s1, 10, 5) | bitnum_intr(s0, 10, 4) | bitnum_intr(s1, 18, 3) | bitnum_intr(s0, 18, 2) | bitnum_intr(s1, 26, 1) | bitnum_intr(s0, 26, 0)) & 255;
    out[5] = (bitnum_intr(s1, 1, 7) | bitnum_intr(s0, 1, 6) | bitnum_intr(s1, 9, 5) | bitnum_intr(s0, 9, 4) | bitnum_intr(s1, 17, 3) | bitnum_intr(s0, 17, 2) | bitnum_intr(s1, 25, 1) | bitnum_intr(s0, 25, 0)) & 255;
    out[4] = (bitnum_intr(s1, 0, 7) | bitnum_intr(s0, 0, 6) | bitnum_intr(s1, 8, 5) | bitnum_intr(s0, 8, 4) | bitnum_intr(s1, 16, 3) | bitnum_intr(s0, 16, 2) | bitnum_intr(s1, 24, 1) | bitnum_intr(s0, 24, 0)) & 255;
  };
  var des_f = (state, key) => {
    const t1 = (bitnum_intl(state, 31, 0) | (state & 4026531840) >>> 0 >>> 1 | bitnum_intl(state, 4, 5) | bitnum_intl(state, 3, 6) | (state & 251658240) >>> 0 >>> 3 | bitnum_intl(state, 8, 11) | bitnum_intl(state, 7, 12) | (state & 15728640) >>> 0 >>> 5 | bitnum_intl(state, 12, 17) | bitnum_intl(state, 11, 18) | (state & 983040) >>> 0 >>> 7 | bitnum_intl(state, 16, 23)) >>> 0;
    const t2 = (bitnum_intl(state, 15, 0) | (state & 61440) << 15 >>> 0 | bitnum_intl(state, 20, 5) | bitnum_intl(state, 19, 6) | (state & 3840) << 13 >>> 0 | bitnum_intl(state, 24, 11) | bitnum_intl(state, 23, 12) | (state & 240) << 11 >>> 0 | bitnum_intl(state, 28, 17) | bitnum_intl(state, 27, 18) | (state & 15) << 9 >>> 0 | bitnum_intl(state, 0, 23)) >>> 0;
    const lrgstate = [
      t1 >>> 24 & 255,
      t1 >>> 16 & 255,
      t1 >>> 8 & 255,
      t2 >>> 24 & 255,
      t2 >>> 16 & 255,
      t2 >>> 8 & 255
    ];
    for (let i = 0; i < 6; i++) lrgstate[i] ^= key[i];
    let s = (sbox[0][sbox_bit(lrgstate[0] >>> 2)] << 28 | sbox[1][sbox_bit((lrgstate[0] & 3) << 4 | lrgstate[1] >>> 4)] << 24 | sbox[2][sbox_bit((lrgstate[1] & 15) << 2 | lrgstate[2] >>> 6)] << 20 | sbox[3][sbox_bit(lrgstate[2] & 63)] << 16 | sbox[4][sbox_bit(lrgstate[3] >>> 2)] << 12 | sbox[5][sbox_bit((lrgstate[3] & 3) << 4 | lrgstate[4] >>> 4)] << 8 | sbox[6][sbox_bit((lrgstate[4] & 15) << 2 | lrgstate[5] >>> 6)] << 4 | sbox[7][sbox_bit(lrgstate[5] & 63)]) >>> 0;
    return (bitnum_intl(s, 15, 0) | bitnum_intl(s, 6, 1) | bitnum_intl(s, 19, 2) | bitnum_intl(s, 20, 3) | bitnum_intl(s, 28, 4) | bitnum_intl(s, 11, 5) | bitnum_intl(s, 27, 6) | bitnum_intl(s, 16, 7) | bitnum_intl(s, 0, 8) | bitnum_intl(s, 14, 9) | bitnum_intl(s, 22, 10) | bitnum_intl(s, 25, 11) | bitnum_intl(s, 4, 12) | bitnum_intl(s, 17, 13) | bitnum_intl(s, 30, 14) | bitnum_intl(s, 9, 15) | bitnum_intl(s, 1, 16) | bitnum_intl(s, 7, 17) | bitnum_intl(s, 23, 18) | bitnum_intl(s, 13, 19) | bitnum_intl(s, 31, 20) | bitnum_intl(s, 26, 21) | bitnum_intl(s, 2, 22) | bitnum_intl(s, 8, 23) | bitnum_intl(s, 18, 24) | bitnum_intl(s, 12, 25) | bitnum_intl(s, 29, 26) | bitnum_intl(s, 5, 27) | bitnum_intl(s, 21, 28) | bitnum_intl(s, 10, 29) | bitnum_intl(s, 3, 30) | bitnum_intl(s, 24, 31)) >>> 0;
  };
  var des_crypt = (input, schedule, output) => {
    let [s0, s1] = initial_permutation(input);
    for (let i = 0; i < 15; i++) {
      const prev = s1;
      s1 = (des_f(s1, schedule[i]) ^ s0) >>> 0;
      s0 = prev;
    }
    s0 = (des_f(s1, schedule[15]) ^ s0) >>> 0;
    inverse_permutation(s0, s1, output);
  };
  var key_schedule = (key, mode) => {
    const schedule = Array.from({ length: 16 }, () => new Uint8Array(6));
    let c = 0;
    let d = 0;
    for (let i = 0; i < 28; i++) {
      c = (c | bitnum(key, key_perm_c[i], 31 - i)) >>> 0;
      d = (d | bitnum(key, key_perm_d[i], 31 - i)) >>> 0;
    }
    for (let i = 0; i < 16; i++) {
      c = ((c << key_rnd_shift[i] >>> 0 | c >>> 28 - key_rnd_shift[i]) & 4294967280) >>> 0;
      d = ((d << key_rnd_shift[i] >>> 0 | d >>> 28 - key_rnd_shift[i]) & 4294967280) >>> 0;
      const togen = mode === DES_DECRYPT ? 15 - i : i;
      for (let j = 0; j < 24; j++) {
        schedule[togen][j / 8 | 0] |= bitnum_intr(c, key_compression[j], 7 - j % 8);
      }
      for (let j = 24; j < 48; j++) {
        schedule[togen][j / 8 | 0] |= bitnum_intr(d, key_compression[j] - 27, 7 - j % 8);
      }
    }
    return schedule;
  };
  var tripledes_key_setup = (key, mode) => {
    if (mode === DES_ENCRYPT) {
      return [
        key_schedule(key.subarray(0, 8), DES_ENCRYPT),
        key_schedule(key.subarray(8, 16), DES_DECRYPT),
        key_schedule(key.subarray(16, 24), DES_ENCRYPT)
      ];
    }
    return [
      key_schedule(key.subarray(16, 24), DES_DECRYPT),
      key_schedule(key.subarray(8, 16), DES_ENCRYPT),
      key_schedule(key.subarray(0, 8), DES_DECRYPT)
    ];
  };
  var tripledes_crypt = (input, schedule, output) => {
    const buf = new Uint8Array(8);
    des_crypt(input, schedule[0], buf);
    des_crypt(buf, schedule[1], output);
    des_crypt(output, schedule[2], buf);
    output.set(buf);
  };
  var QRC_KEY = Buffer.from([
    33,
    64,
    35,
    41,
    40,
    42,
    36,
    37,
    49,
    50,
    51,
    90,
    88,
    67,
    33,
    64,
    33,
    64,
    35,
    41,
    40,
    78,
    72,
    76
  ]);
  var handleInflate2 = (data) => new Promise((resolve, reject) => {
    resolve(inflate(data, { finishFlush: constants.Z_SYNC_FLUSH }));
  });
  var decodeQrc = async (hexData) => {
    if (!hexData || hexData.length % 2 !== 0) return "";
    const encrypted = Buffer.from(hexData, "hex");
    if (encrypted.length === 0) return "";
    const schedule = tripledes_key_setup(QRC_KEY, DES_DECRYPT);
    const block = new Uint8Array(8);
    for (let i = 0; i + 8 <= encrypted.length; i += 8) {
      tripledes_crypt(encrypted.subarray(i, i + 8), schedule, block);
      encrypted.set(block, i);
    }
    try {
      const result = await handleInflate2(encrypted);
      return Buffer.from(result).toString("utf-8");
    } catch {
      return "";
    }
  };

  // src/utils/musicSdk/tx/lyric.js
  var songIdMap = /* @__PURE__ */ new Map();
  var promises = /* @__PURE__ */ new Map();
  var decodeLyric3 = async (lrc, tlrc, rlrc) => ({
    lyric: await decodeQrc(lrc),
    tlyric: await decodeQrc(tlrc),
    rlyric: await decodeQrc(rlrc)
  });
  var parseTools = {
    rxps: {
      info: /^{"/,
      lineTime: /^\[(\d+),\d+\]/,
      lineTime2: /^\[([\d:.]+)\]/,
      wordTime: /\(\d+,\d+\)/,
      wordTimeAll: /(\(\d+,\d+\))/g,
      timeLabelFixRxp: /(?:\.0+|0+)$/
    },
    msFormat(timeMs) {
      if (Number.isNaN(timeMs)) return "";
      let ms = timeMs % 1e3;
      timeMs /= 1e3;
      let m = parseInt(timeMs / 60).toString().padStart(2, "0");
      timeMs %= 60;
      let s = parseInt(timeMs).toString().padStart(2, "0");
      return `[${m}:${s}.${String(ms).padStart(3, "0")}]`;
    },
    parseLyric(lrc) {
      lrc = lrc.trim();
      lrc = lrc.replace(/\r/g, "");
      if (!lrc) return { lyric: "", lxlyric: "" };
      const lines = lrc.split("\n");
      const lxlrcLines = [];
      const lrcLines = [];
      for (let line of lines) {
        line = line.trim();
        let result = this.rxps.lineTime.exec(line);
        if (!result) {
          if (line.startsWith("[offset")) {
            lxlrcLines.push(line);
            lrcLines.push(line);
          }
          if (this.rxps.lineTime2.test(line)) {
            lrcLines.push(line);
          }
          continue;
        }
        const startMsTime = parseInt(result[1]);
        const startTimeStr = this.msFormat(startMsTime);
        if (!startTimeStr) continue;
        let words = line.replace(this.rxps.lineTime, "");
        lrcLines.push(`${startTimeStr}${words.replace(this.rxps.wordTimeAll, "")}`);
        let times = words.match(this.rxps.wordTimeAll);
        if (!times) continue;
        times = times.map((time) => {
          const result2 = /\((\d+),(\d+)\)/.exec(time);
          return `<${Math.max(parseInt(result2[1]) - startMsTime, 0)},${result2[2]}>`;
        });
        const wordArr = words.split(this.rxps.wordTime);
        const newWords = times.map((time, index) => `${time}${wordArr[index]}`).join("");
        lxlrcLines.push(`${startTimeStr}${newWords}`);
      }
      return {
        lyric: lrcLines.join("\n"),
        lxlyric: lxlrcLines.join("\n")
      };
    },
    parseRlyric(lrc) {
      lrc = lrc.trim();
      lrc = lrc.replace(/\r/g, "");
      if (!lrc) return { lyric: "", lxlyric: "" };
      const lines = lrc.split("\n");
      const lrcLines = [];
      for (let line of lines) {
        line = line.trim();
        let result = this.rxps.lineTime.exec(line);
        if (!result) continue;
        const startMsTime = parseInt(result[1]);
        const startTimeStr = this.msFormat(startMsTime);
        if (!startTimeStr) continue;
        let words = line.replace(this.rxps.lineTime, "");
        lrcLines.push(`${startTimeStr}${words.replace(this.rxps.wordTimeAll, "")}`);
      }
      return lrcLines.join("\n");
    },
    removeTag(str) {
      return str.replace(/^[\S\s]*?LyricContent="/, "").replace(/"\/>[\S\s]*?$/, "");
    },
    getIntv(interval) {
      if (!interval) return 0;
      if (!interval.includes(".")) interval += ".0";
      let arr = interval.split(/:|\./);
      while (arr.length < 3) arr.unshift("0");
      const [m, s, ms] = arr;
      return parseInt(m) * 36e5 + parseInt(s) * 1e3 + parseInt(ms);
    },
    fixRlrcTimeTag(rlrc, lrc) {
      const rlrcLines = rlrc.split("\n");
      let lrcLines = lrc.split("\n");
      let newLrc = [];
      rlrcLines.forEach((line) => {
        const result = this.rxps.lineTime2.exec(line);
        if (!result) return;
        const words = line.replace(this.rxps.lineTime2, "");
        if (!words.trim()) return;
        const t1 = this.getIntv(result[1]);
        while (lrcLines.length) {
          const lrcLine = lrcLines.shift();
          const lrcLineResult = this.rxps.lineTime2.exec(lrcLine);
          if (!lrcLineResult) continue;
          const t2 = this.getIntv(lrcLineResult[1]);
          if (Math.abs(t1 - t2) < 100) {
            newLrc.push(line.replace(this.rxps.lineTime2, lrcLineResult[0]));
            break;
          }
        }
      });
      return newLrc.join("\n");
    },
    fixTlrcTimeTag(tlrc, lrc) {
      const tlrcLines = tlrc.split("\n");
      let lrcLines = lrc.split("\n");
      let newLrc = [];
      tlrcLines.forEach((line) => {
        const result = this.rxps.lineTime2.exec(line);
        if (!result) return;
        const words = line.replace(this.rxps.lineTime2, "");
        if (!words.trim()) return;
        let time = result[1];
        if (time.includes(".")) {
          time += "".padStart(3 - time.split(".")[1].length, "0");
        }
        const t1 = this.getIntv(time);
        while (lrcLines.length) {
          const lrcLine = lrcLines.shift();
          const lrcLineResult = this.rxps.lineTime2.exec(lrcLine);
          if (!lrcLineResult) continue;
          const t2 = this.getIntv(lrcLineResult[1]);
          if (Math.abs(t1 - t2) < 100) {
            newLrc.push(line.replace(this.rxps.lineTime2, lrcLineResult[0]));
            break;
          }
        }
      });
      return newLrc.join("\n");
    },
    parse(lrc, tlrc, rlrc) {
      const info = {
        lyric: "",
        tlyric: "",
        rlyric: "",
        lxlyric: ""
      };
      if (lrc) {
        let { lyric, lxlyric } = this.parseLyric(this.removeTag(lrc));
        info.lyric = lyric;
        info.lxlyric = lxlyric;
      }
      if (rlrc) info.rlyric = this.fixRlrcTimeTag(this.parseRlyric(this.removeTag(rlrc)), info.lyric);
      if (tlrc) info.tlyric = this.fixTlrcTimeTag(tlrc, info.lyric);
      return info;
    }
  };
  var lyric_default3 = {
    successCode: 0,
    async getSongId({ songId, songmid }) {
      if (songId) return songId;
      if (songIdMap.has(songmid)) return songIdMap.get(songmid);
      if (promises.has(songmid)) return (await promises.get(songmid)).songId;
      const promise = musicInfo_default(songmid);
      promises.set(promise);
      const info = await promise;
      songIdMap.set(songmid, info.songId);
      promises.delete(songmid);
      return info.songId;
    },
    async parseLyric(lrc, tlrc, rlrc) {
      const { lyric, tlyric, rlyric } = await decodeLyric3(lrc, tlrc, rlrc);
      return parseTools.parse(lyric, tlyric, rlyric);
    },
    getLyric(mInfo, retryNum = 0) {
      if (retryNum > 3) return Promise.reject(new Error("Get lyric failed"));
      return {
        cancelHttp() {
        },
        promise: this.getSongId(mInfo).then((songId) => {
          const requestObj = httpFetch("https://u.y.qq.com/cgi-bin/musicu.fcg", {
            method: "post",
            headers: {
              referer: "https://y.qq.com",
              "user-agent": "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36"
            },
            body: {
              comm: {
                ct: "19",
                cv: "1859",
                uin: "0"
              },
              req: {
                method: "GetPlayLyricInfo",
                module: "music.musichallSong.PlayLyricInfo",
                param: {
                  format: "json",
                  crypt: 1,
                  ct: 19,
                  cv: 1873,
                  interval: 0,
                  lrc_t: 0,
                  qrc: 1,
                  qrc_t: 0,
                  roma: 1,
                  roma_t: 0,
                  songID: songId,
                  trans: 1,
                  trans_t: 0,
                  type: -1
                }
              }
            }
          });
          return requestObj.promise.then(({ body }) => {
            if (body.code != this.successCode || body.req.code != this.successCode) return this.getLyric(songId, ++retryNum);
            const data = body.req.data;
            return this.parseLyric(data.lyric, data.trans, data.roma);
          });
        })
      };
    }
  };

  // src/utils/musicSdk/tx/songList.js
  var songList_default3 = {
    _requestObj_tags: null,
    _requestObj_hotTags: null,
    _requestObj_list: null,
    limit_list: 36,
    limit_song: 1e5,
    successCode: 0,
    sortList: [
      {
        name: "\u63A8\u8350",
        tid: "recommend",
        id: "-1"
      },
      {
        name: "\u6700\u70ED",
        tid: "hot",
        id: "3"
      },
      {
        name: "\u6700\u65B0",
        tid: "new",
        id: "2"
      }
    ],
    regExps: {
      hotTagHtml: /class="c_bg_link js_tag_item" data-id="\w+">.+?<\/a>/g,
      hotTag: /data-id="(\w+)">(.+?)<\/a>/,
      // https://y.qq.com/n/yqq/playlist/7217720898.html
      // https://i.y.qq.com/n2/m/share/details/taoge.html?platform=11&appshare=android_qq&appversion=9050006&id=7217720898&ADTAG=qfshare
      listDetailLink: /\/playlist\/(\d+)/,
      listDetailLink2: /id=(\d+)/
    },
    tagsUrl: "https://u.y.qq.com/cgi-bin/musicu.fcg?loginUin=0&hostUin=0&format=json&inCharset=utf-8&outCharset=utf-8&notice=0&platform=wk_v15.json&needNewCode=0&data=%7B%22tags%22%3A%7B%22method%22%3A%22get_all_categories%22%2C%22param%22%3A%7B%22qq%22%3A%22%22%7D%2C%22module%22%3A%22playlist.PlaylistAllCategoriesServer%22%7D%7D",
    hotTagUrl: "https://c.y.qq.com/node/pc/wk_v15/category_playlist.html",
    getListUrl(id, page) {
      id = parseInt(id);
      return `https://u.y.qq.com/cgi-bin/musicu.fcg?loginUin=0&hostUin=0&format=json&inCharset=utf-8&outCharset=utf-8&notice=0&platform=wk_v15.json&needNewCode=0&data=${encodeURIComponent(JSON.stringify({
        comm: { cv: 1602, ct: 20 },
        playlist: {
          method: "get_category_content",
          param: {
            titleid: id,
            caller: "0",
            category_id: id,
            size: this.limit_list,
            page: page - 1,
            use_page: 1
          },
          module: "playlist.PlayListCategoryServer"
        }
      }))}`;
    },
    getListDetailUrl(id) {
      return `https://c.y.qq.com/qzone/fcg-bin/fcg_ucc_getcdinfo_byids_cp.fcg?type=1&json=1&utf8=1&onlysong=0&new_format=1&disstid=${id}&loginUin=0&hostUin=0&format=json&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq.json&needNewCode=0`;
    },
    // http://nplserver.kuwo.cn/pl.svc?op=getlistinfo&pid=2849349915&pn=0&rn=100&encode=utf8&keyset=pl2012&identity=kuwo&pcmp4=1&vipver=MUSIC_9.0.5.0_W1&newver=1
    // 获取标签
    getTag(tryNum = 0) {
      if (this._requestObj_tags) this._requestObj_tags.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      this._requestObj_tags = httpFetch(this.tagsUrl);
      return this._requestObj_tags.promise.then(({ body }) => {
        if (body.code !== this.successCode) return this.getTag(++tryNum);
        return this.filterTagInfo(body.tags.data.v_group);
      });
    },
    // 获取标签
    getHotTag(tryNum = 0) {
      if (this._requestObj_hotTags) this._requestObj_hotTags.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      this._requestObj_hotTags = httpFetch(this.hotTagUrl);
      return this._requestObj_hotTags.promise.then(({ statusCode, body }) => {
        if (statusCode !== 200) return this.getHotTag(++tryNum);
        return this.filterInfoHotTag(body);
      });
    },
    filterInfoHotTag(html) {
      let hotTag = html.match(this.regExps.hotTagHtml);
      const hotTags = [];
      if (!hotTag) return hotTags;
      hotTag.forEach((tagHtml) => {
        let result = tagHtml.match(this.regExps.hotTag);
        if (!result) return;
        hotTags.push({
          id: parseInt(result[1]),
          name: result[2],
          source: "tx"
        });
      });
      return hotTags;
    },
    filterTagInfo(rawList) {
      return rawList.map((type) => ({
        name: type.group_name,
        list: type.v_item.map((item) => ({
          parent_id: type.group_id,
          parent_name: type.group_name,
          id: item.id,
          name: item.name,
          source: "tx"
        }))
      }));
    },
    async getRecommendList(page) {
      const { body } = await httpFetch(
        `https://u.y.qq.com/cgi-bin/musicu.fcg?loginUin=0&hostUin=0&format=json&inCharset=utf-8&outCharset=utf-8&notice=0&platform=wk_v15.json&needNewCode=0&data=${encodeURIComponent(
          JSON.stringify({
            comm: { cv: 1602, ct: 20 },
            playlist: {
              module: "music.playlist.PlaylistSquare",
              method: "GetRecommendWhole",
              param: {
                IsReqFeed: true,
                FeedReq: {
                  From: (page - 1) * this.limit_list,
                  Size: this.limit_list
                }
              }
            }
          })
        )}`
      ).promise;
      if (body.code !== this.successCode) throw new Error("tx getRecommendList failed");
      return {
        list: body.playlist.data.FeedRsp.List?.map(({ Playlist }) => ({
          play_count: formatPlayCount(Playlist.basic.play_cnt),
          id: String(Playlist.basic.tid),
          author: decodeName(Playlist.basic.creator.nick),
          name: decodeName(Playlist.basic.title),
          time: Playlist.basic.modify_time ? dateFormat(Playlist.basic.modify_time * 1e3, "Y-M-D") : "",
          img: Playlist.basic.cover.medium_url || Playlist.basic.cover.default_url,
          total: Playlist.basic.song_cnt,
          desc: decodeName(Playlist.basic.desc).replace(/<br>/g, "\n"),
          source: "tx"
        })) || [],
        total: body.playlist.data.FeedRsp.FromLimit,
        limit: this.limit_list,
        page,
        source: "tx"
      };
    },
    // 获取列表数据
    getList(sortId, tagId, page, tryNum = 0) {
      if (this._requestObj_list) this._requestObj_list.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      if (!tagId) {
        if (!tagId && sortId == -1) return this.getRecommendList(page);
        tagId = sortId;
      }
      this._requestObj_list = httpFetch(
        this.getListUrl(tagId, page)
      );
      return this._requestObj_list.promise.then(({ body }) => {
        if (body.code !== this.successCode) return this.getList(sortId, tagId, page, ++tryNum);
        return this.filterList(body.playlist.data, page);
      });
    },
    // filterList(data, page) {
    //   return {
    //     list: data.v_playlist.map(item => ({
    //       play_count: formatPlayCount(item.access_num),
    //       id: String(item.tid),
    //       author: item.creator_info.nick,
    //       name: item.title,
    //       time: item.modify_time ? dateFormat(item.modify_time * 1000, 'Y-M-D') : '',
    //       img: item.cover_url_medium,
    //       // grade: item.favorcnt / 10,
    //       total: item.song_ids?.length,
    //       desc: decodeName(item.desc).replace(/<br>/g, '\n'),
    //       source: 'tx',
    //     })),
    //     total: data.total,
    //     page,
    //     limit: this.limit_list,
    //     source: 'tx',
    //   }
    // },
    filterList({ content }, page) {
      return {
        list: content.v_item.map(({ basic }) => ({
          play_count: formatPlayCount(basic.play_cnt),
          id: String(basic.tid),
          author: basic.creator.nick,
          name: basic.title,
          // time: basic.publish_time,
          img: basic.cover.medium_url || basic.cover.default_url,
          // grade: basic.favorcnt / 10,
          desc: decodeName(basic.desc).replace(/<br>/g, "\n"),
          source: "tx"
        })),
        total: content.total_cnt,
        page,
        limit: this.limit_list,
        source: "tx"
      };
    },
    async handleParseId(link, retryNum = 0) {
      if (retryNum > 2) return Promise.reject(new Error("link try max num"));
      const requestObj_listDetailLink = httpFetch(link);
      const { url, statusCode } = await requestObj_listDetailLink.promise;
      if (statusCode > 400) return this.handleParseId(link, ++retryNum);
      return url;
    },
    async getListId(id) {
      if (/[?&:/]/.test(id)) {
        if (!this.regExps.listDetailLink.test(id)) {
          id = await this.handleParseId(id);
        }
        let result = this.regExps.listDetailLink.exec(id);
        if (!result) {
          result = this.regExps.listDetailLink2.exec(id);
          if (!result) throw new Error("failed");
        }
        id = result[1];
      }
      return id;
    },
    // 获取歌曲列表内的音乐
    async getListDetail2(id, tryNum = 0) {
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      const requestObj_listDetail = httpFetch("https://u.y.qq.com/cgi-bin/musicu.fcg", {
        method: "post",
        headers: {
          Origin: "https://y.qq.com",
          Referer: `https://y.qq.com/n/yqq/playsquare/${id}.html`
        },
        body: {
          comm: {
            cv: 4747474,
            ct: 24,
            format: "json",
            inCharset: "utf-8",
            outCharset: "utf-8",
            platform: "yqq.json",
            needNewCode: 1,
            uin: 0
          },
          req_1: {
            module: "music.srfDissInfo.aiDissInfo",
            method: "uniform_get_Dissinfo",
            param: {
              disstid: parseInt(id),
              userinfo: 1,
              tag: 1,
              orderlist: 1,
              song_begin: 0,
              song_num: this.limit_song,
              onlysonglist: 0,
              enc_host_uin: ""
            }
          }
        }
      });
      const { body } = await requestObj_listDetail.promise;
      if (body.code !== this.successCode) return this.getListDetail2(id, ++tryNum);
      if (body.req_1.code !== this.successCode) throw new Error("failed");
      const result = body.req_1.data;
      const dirinfo = result.dirinfo;
      return {
        list: this.filterListDetail(result.songlist),
        page: 1,
        limit: this.limit_song,
        total: result.total_song_num,
        source: "tx",
        info: {
          name: dirinfo.title,
          img: dirinfo.picurl,
          desc: decodeName(dirinfo.desc ?? "").replace(/<br>/g, "\n"),
          author: dirinfo.host_nick,
          play_count: formatPlayCount(dirinfo.listennum)
        }
      };
    },
    // 获取歌曲列表内的音乐
    async getListDetail(id, tryNum = 0) {
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      id = await this.getListId(id);
      const requestObj_listDetail = httpFetch(this.getListDetailUrl(id), {
        headers: {
          Origin: "https://y.qq.com",
          Referer: `https://y.qq.com/n/yqq/playsquare/${id}.html`
        }
      });
      const { body } = await requestObj_listDetail.promise;
      if (body.code !== this.successCode) return this.getListDetail(id, ++tryNum);
      if (body.subcode !== this.successCode || !body.cdlist) return this.getListDetail2(id);
      const cdlist = body.cdlist[0];
      return {
        list: this.filterListDetail(cdlist.songlist),
        page: 1,
        limit: cdlist.songlist.length + 1,
        total: cdlist.songlist.length,
        source: "tx",
        info: {
          name: cdlist.dissname,
          img: cdlist.logo,
          desc: decodeName(cdlist.desc).replace(/<br>/g, "\n"),
          author: cdlist.nickname,
          play_count: formatPlayCount(cdlist.visitnum)
        }
      };
    },
    filterListDetail(rawList) {
      return rawList.map((item) => {
        let types = [];
        let _types = {};
        if (item.file.size_128mp3 !== 0) {
          let size = sizeFormate(item.file.size_128mp3);
          types.push({ type: "128k", size });
          _types["128k"] = {
            size
          };
        }
        if (item.file.size_320mp3 !== 0) {
          let size = sizeFormate(item.file.size_320mp3);
          types.push({ type: "320k", size });
          _types["320k"] = {
            size
          };
        }
        if (item.file.size_flac !== 0) {
          let size = sizeFormate(item.file.size_flac);
          types.push({ type: "flac", size });
          _types.flac = {
            size
          };
        }
        if (item.file.size_hires !== 0) {
          let size = sizeFormate(item.file.size_hires);
          types.push({ type: "flac24bit", size });
          _types.flac24bit = {
            size
          };
        }
        return {
          singer: formatSingerName(item.singer, "name"),
          name: item.title,
          albumName: item.album.name,
          albumId: item.album.mid,
          source: "tx",
          interval: formatPlayTime(item.interval),
          songId: item.id,
          albumMid: item.album.mid,
          strMediaMid: item.file.media_mid,
          songmid: item.mid,
          img: item.album.name === "" || item.album.name === "\u7A7A" ? item.singer?.length ? `https://y.gtimg.cn/music/photo_new/T001R500x500M000${item.singer[0].mid}.jpg` : "" : `https://y.gtimg.cn/music/photo_new/T002R500x500M000${item.album.mid}.jpg`,
          lrc: null,
          otherSource: null,
          types,
          _types,
          typeUrl: {}
        };
      });
    },
    getTags() {
      return Promise.all([this.getTag(), this.getHotTag()]).then(([tags, hotTag]) => ({ tags, hotTag, source: "tx" }));
    },
    async getDetailPageUrl(id) {
      id = await this.getListId(id);
      return `https://y.qq.com/n/ryqq/playlist/${id}`;
    },
    search(text, page, limit = 20, retryNum = 0) {
      if (retryNum > 5) throw new Error("max retry");
      return httpFetch(`http://c.y.qq.com/soso/fcgi-bin/client_music_search_songlist?page_no=${page - 1}&num_per_page=${limit}&format=json&query=${encodeURIComponent(text)}&remoteplace=txt.yqq.playlist&inCharset=utf8&outCharset=utf-8`, {
        headers: {
          "User-Agent": "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)",
          Referer: "http://y.qq.com/portal/search.html"
        }
      }).promise.then(({ body }) => {
        if (body.code != 0) return this.search(text, page, limit, ++retryNum);
        return {
          list: body.data.list.map((item) => {
            return {
              play_count: formatPlayCount(item.listennum),
              id: String(item.dissid),
              author: decodeName(item.creator.name),
              name: decodeName(item.dissname),
              time: dateFormat(item.createtime, "Y-M-D"),
              img: item.imgurl,
              // grade: item.favorcnt / 10,
              total: item.song_count,
              desc: decodeName(decodeName(item.introduction)).replace(/<br>/g, "\n"),
              source: "tx"
            };
          }),
          limit,
          total: body.data.sum,
          source: "tx"
        };
      });
    }
  };

  // src/utils/musicSdk/tx/utils/crypto.js
  var PART_1_INDEXES = [23, 14, 6, 36, 16, 40, 7, 19];
  var PART_2_INDEXES = [16, 1, 32, 12, 19, 27, 8, 5];
  var SCRAMBLE_VALUES = [89, 39, 179, 150, 218, 82, 58, 252, 177, 52, 186, 123, 120, 64, 242, 133, 143, 161, 121, 179];
  function pickHashByIdx(hash, indexes) {
    return indexes.map((idx) => hash[idx]).join("");
  }
  function base64Encode(data) {
    return Buffer.from(data).toString("base64").replace(/[\\/+=]/g, "");
  }
  async function zzcSign(text) {
    const hash = await hashSHA1(text);
    const part1 = pickHashByIdx(hash, PART_1_INDEXES);
    const part2 = pickHashByIdx(hash, PART_2_INDEXES);
    const part3 = SCRAMBLE_VALUES.map((value, i) => value ^ parseInt(hash.slice(i * 2, i * 2 + 2), 16));
    const b64Part = base64Encode(part3).replace(/[\\/+=]/g, "");
    return `zzc${part1}${b64Part}${part2}`.toLowerCase();
  }

  // src/utils/musicSdk/tx/utils/index.js
  var signRequest = async (data) => {
    const sign = await zzcSign(JSON.stringify(data));
    return httpFetch(`https://u.y.qq.com/cgi-bin/musics.fcg?sign=${sign}`, {
      method: "post",
      headers: {
        "User-Agent": "QQMusic 14090508(android 12)"
      },
      body: data
    }).promise;
  };

  // src/utils/musicSdk/tx/musicSearch.js
  var musicSearch_default3 = {
    limit: 50,
    total: 0,
    page: 0,
    allPage: 1,
    successCode: 0,
    musicSearch(str, page, limit, retryNum = 0) {
      if (retryNum > 5) return Promise.reject(new Error("\u641C\u7D22\u5931\u8D25"));
      const searchRequest = signRequest({
        comm: {
          _channelid: "0",
          _os_version: "6.2.9200-2",
          ct: "19",
          cv: "2151",
          guid: "1F70E520B2EAA7D25E11760783C53CA9",
          patch: "118",
          psrf_access_token_expiresAt: 0,
          psrf_qqaccess_token: "",
          psrf_qqopenid: "",
          psrf_qqunionid: "",
          tmeAppID: "qqmusic",
          tmeLoginType: 0,
          uin: "0",
          wid: "7223299733393904640"
        },
        "music.search.SearchCgiService": {
          module: "music.search.SearchCgiService",
          method: "DoSearchForQQMusicDesktop",
          param: {
            grp: 1,
            num_per_page: limit,
            page_num: page,
            query: str,
            remoteplace: "txt.newclient.top",
            search_type: 0,
            searchid: this.getSearchId()
          }
        }
      });
      return searchRequest.then(({ body }) => {
        const req = body?.["music.search.SearchCgiService"] ?? body?.req;
        if (!req || body.code != this.successCode || req.code != this.successCode) {
          return this.musicSearch(str, page, limit, ++retryNum);
        }
        return req.data;
      });
    },
    /**
     * PC 客户端版 searchid：32 位大写十六进制 GUID + 5 位补零随机数 = 37 字符。
     * 对应 QQ 音乐 PC 端 searchid 形状（服务端只需要唯一的会话 ID，形状一致即可）。
     */
    getSearchId() {
      let guid = "";
      for (let i = 0; i < 32; i++) guid += Math.floor(Math.random() * 16).toString(16);
      return guid.toUpperCase() + String(Math.floor(Math.random() * 1e5)).padStart(5, "0");
    },
    handleResult(rawList) {
      if (!rawList || !Array.isArray(rawList)) return [];
      const list = [];
      rawList.forEach((item) => {
        if (!item.file?.media_mid) return;
        let types = [];
        let _types = {};
        const file = item.file;
        if (file.size_128mp3 != 0) {
          let size = sizeFormate(file.size_128mp3);
          types.push({ type: "128k", size });
          _types["128k"] = {
            size
          };
        }
        if (file.size_320mp3 !== 0) {
          let size = sizeFormate(file.size_320mp3);
          types.push({ type: "320k", size });
          _types["320k"] = {
            size
          };
        }
        if (file.size_flac !== 0) {
          let size = sizeFormate(file.size_flac);
          types.push({ type: "flac", size });
          _types.flac = {
            size
          };
        }
        if (file.size_hires !== 0) {
          let size = sizeFormate(file.size_hires);
          types.push({ type: "flac24bit", size });
          _types.flac24bit = {
            size
          };
        }
        let albumId = "";
        let albumName = "";
        if (item.album) {
          albumName = item.album.name;
          albumId = item.album.mid;
        }
        list.push({
          singer: formatSingerName(item.singer, "name"),
          // name: item.name + (item.title_extra ?? ''),
          name: item.title,
          albumName,
          albumId,
          source: "tx",
          interval: item.interval ? formatPlayTime(item.interval) : null,
          songId: item.id,
          albumMid: item.album?.mid ?? "",
          strMediaMid: item.file.media_mid,
          songmid: item.mid,
          img: albumId === "" || albumId === "\u7A7A" ? item.singer?.length ? `https://y.gtimg.cn/music/photo_new/T001R500x500M000${item.singer[0].mid}.jpg` : "" : `https://y.gtimg.cn/music/photo_new/T002R500x500M000${albumId}.jpg`,
          types,
          _types,
          typeUrl: {}
        });
      });
      return list;
    },
    search(str, page = 1, limit) {
      if (limit == null) limit = this.limit;
      return this.musicSearch(str, page, limit).then(({ body, meta }) => {
        let list = this.handleResult(body.song.list);
        this.total = meta.sum;
        this.page = page;
        this.allPage = Math.ceil(this.total / limit);
        return Promise.resolve({
          list,
          allPage: this.allPage,
          limit,
          total: this.total,
          source: "tx"
        });
      });
    }
  };

  // src/utils/musicSdk/tx/hotSearch.js
  var hotSearch_default3 = {
    _requestObj: null,
    async getList(retryNum = 0) {
      if (this._requestObj) this._requestObj.cancelHttp();
      if (retryNum > 2) return Promise.reject(new Error("try max num"));
      const _requestObj = httpFetch("https://u.y.qq.com/cgi-bin/musicu.fcg", {
        method: "post",
        body: {
          comm: {
            ct: "19",
            cv: "1803",
            guid: "0",
            patch: "118",
            psrf_access_token_expiresAt: 0,
            psrf_qqaccess_token: "",
            psrf_qqopenid: "",
            psrf_qqunionid: "",
            tmeAppID: "qqmusic",
            tmeLoginType: 0,
            uin: "0",
            wid: "0"
          },
          hotkey: {
            method: "GetHotkeyForQQMusicPC",
            module: "tencent_musicsoso_hotkey.HotkeyService",
            param: {
              search_id: "",
              uin: 0
            }
          }
        },
        headers: {
          Referer: "https://y.qq.com/portal/player.html"
        }
      });
      const { body, statusCode } = await _requestObj.promise;
      if (statusCode != 200 || body.code !== 0) throw new Error("\u83B7\u53D6\u70ED\u641C\u8BCD\u5931\u8D25");
      return { source: "tx", list: this.filterList(body.hotkey.data.vec_hotkey) };
    },
    filterList(rawList) {
      return rawList.map((item) => item.query);
    }
  };

  // src/utils/musicSdk/tx/comment.js
  var emojis = {
    e400846: "\u{1F618}",
    e400874: "\u{1F634}",
    e400825: "\u{1F603}",
    e400847: "\u{1F619}",
    e400835: "\u{1F60D}",
    e400873: "\u{1F633}",
    e400836: "\u{1F60E}",
    e400867: "\u{1F62D}",
    e400832: "\u{1F60A}",
    e400837: "\u{1F60F}",
    e400875: "\u{1F62B}",
    e400831: "\u{1F609}",
    e400855: "\u{1F621}",
    e400823: "\u{1F604}",
    e400862: "\u{1F628}",
    e400844: "\u{1F616}",
    e400841: "\u{1F613}",
    e400830: "\u{1F608}",
    e400828: "\u{1F606}",
    e400833: "\u{1F60B}",
    e400822: "\u{1F600}",
    e400843: "\u{1F615}",
    e400829: "\u{1F607}",
    e400824: "\u{1F602}",
    e400834: "\u{1F60C}",
    e400877: "\u{1F637}",
    e400132: "\u{1F349}",
    e400181: "\u{1F37A}",
    e401067: "\u2615\uFE0F",
    e400186: "\u{1F967}",
    e400343: "\u{1F437}",
    e400116: "\u{1F339}",
    e400126: "\u{1F343}",
    e400613: "\u{1F48B}",
    e401236: "\u2764\uFE0F",
    e400622: "\u{1F494}",
    e400637: "\u{1F4A3}",
    e400643: "\u{1F4A9}",
    e400773: "\u{1F52A}",
    e400102: "\u{1F31B}",
    e401328: "\u{1F31E}",
    e400420: "\u{1F44F}",
    e400914: "\u{1F64C}",
    e400408: "\u{1F44D}",
    e400414: "\u{1F44E}",
    e401121: "\u270B",
    e400396: "\u{1F44B}",
    e400384: "\u{1F449}",
    e401115: "\u270A",
    e400402: "\u{1F44C}",
    e400905: "\u{1F648}",
    e400906: "\u{1F649}",
    e400907: "\u{1F64A}",
    e400562: "\u{1F47B}",
    e400932: "\u{1F64F}",
    e400644: "\u{1F4AA}",
    e400611: "\u{1F489}",
    e400185: "\u{1F381}",
    e400655: "\u{1F4B0}",
    e400325: "\u{1F425}",
    e400612: "\u{1F48A}",
    e400198: "\u{1F389}",
    e401685: "\u26A1\uFE0F",
    e400631: "\u{1F49D}",
    e400768: "\u{1F525}",
    e400432: "\u{1F451}"
  };
  var songIdMap2 = /* @__PURE__ */ new Map();
  var promises2 = /* @__PURE__ */ new Map();
  var comment_default3 = {
    _requestObj: null,
    _requestObj2: null,
    async getSongId({ songId, songmid }) {
      if (songId) return songId;
      if (songIdMap2.has(songmid)) return songIdMap2.get(songmid);
      if (promises2.has(songmid)) return (await promises2.get(songmid)).songId;
      const promise = musicInfo_default(songmid);
      promises2.set(promise);
      const info = await promise;
      songIdMap2.set(songmid, info.songId);
      promises2.delete(songmid);
      return info.songId;
    },
    async getComment(mInfo, page = 1, limit = 20) {
      if (this._requestObj) this._requestObj.cancelHttp();
      const songId = await this.getSongId(mInfo);
      const _requestObj = httpFetch("http://c.y.qq.com/base/fcgi-bin/fcg_global_comment_h5.fcg", {
        method: "POST",
        headers: {
          "User-Agent": "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)"
        },
        form: {
          uin: "0",
          format: "json",
          cid: "205360772",
          reqtype: "2",
          biztype: "1",
          topid: songId,
          cmd: "8",
          needmusiccrit: "1",
          pagenum: page - 1,
          pagesize: limit
        }
      });
      const { body, statusCode } = await _requestObj.promise;
      if (statusCode != 200 || body.code !== 0) throw new Error("\u83B7\u53D6\u8BC4\u8BBA\u5931\u8D25");
      const comment = body.comment;
      return {
        source: "tx",
        comments: this.filterNewComment(comment.commentlist),
        total: comment.commenttotal,
        page,
        limit,
        maxPage: Math.ceil(comment.commenttotal / limit) || 1
      };
    },
    async getHotComment(mInfo, page = 1, limit = 20) {
      if (this._requestObj2) this._requestObj2.cancelHttp();
      const songId = await this.getSongId(mInfo);
      const _requestObj2 = httpFetch("https://u.y.qq.com/cgi-bin/musicu.fcg", {
        method: "POST",
        body: {
          comm: {
            cv: 4747474,
            ct: 24,
            format: "json",
            inCharset: "utf-8",
            outCharset: "utf-8",
            notice: 0,
            platform: "yqq.json",
            needNewCode: 1,
            uin: 0
          },
          req: {
            module: "music.globalComment.CommentRead",
            method: "GetHotCommentList",
            param: {
              BizType: 1,
              BizId: String(songId),
              LastCommentSeqNo: "",
              PageSize: limit,
              PageNum: page - 1,
              HotType: 1,
              WithAirborne: 0,
              PicEnable: 1
            }
          }
        },
        headers: {
          "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36 Edg/113.0.0.0",
          referer: "https://y.qq.com/",
          origin: "https://y.qq.com"
        }
      });
      const { body, statusCode } = await _requestObj2.promise;
      if (statusCode != 200 || body.code !== 0 || body.req.code !== 0) throw new Error("\u83B7\u53D6\u70ED\u95E8\u8BC4\u8BBA\u5931\u8D25");
      const comment = body.req.data.CommentList;
      return {
        source: "tx",
        comments: this.filterHotComment(comment.Comments),
        total: comment.Total,
        page,
        limit,
        maxPage: Math.ceil(comment.Total / limit) || 1
      };
    },
    filterNewComment(rawList) {
      return rawList.map((item) => {
        let time = this.formatTime(item.time);
        let timeStr = time ? dateFormat2(time) : null;
        if (item.middlecommentcontent) {
          let firstItem = item.middlecommentcontent[0];
          firstItem.avatarurl = item.avatarurl;
          firstItem.praisenum = item.praisenum;
          item.avatarurl = null;
          item.praisenum = null;
          item.middlecommentcontent.reverse();
        }
        return {
          id: `${item.rootcommentid}_${item.commentid}`,
          rootId: item.rootcommentid,
          text: item.rootcommentcontent ? this.replaceEmoji(item.rootcommentcontent).replace(/\\n/g, "\n") : "",
          time: item.rootcommentid == item.commentid ? time : null,
          timeStr: item.rootcommentid == item.commentid ? timeStr : null,
          userName: item.rootcommentnick ? item.rootcommentnick.substring(1) : "",
          avatar: item.avatarurl,
          userId: item.encrypt_rootcommentuin,
          likedCount: item.praisenum,
          reply: item.middlecommentcontent ? item.middlecommentcontent.map((c) => {
            return {
              id: `sub_${item.rootcommentid}_${c.subcommentid}`,
              text: this.replaceEmoji(c.subcommentcontent).replace(/\\n/g, "\n"),
              time: c.subcommentid == item.commentid ? time : null,
              timeStr: c.subcommentid == item.commentid ? timeStr : null,
              userName: c.replynick.substring(1),
              avatar: c.avatarurl,
              userId: c.encrypt_replyuin,
              likedCount: c.praisenum
            };
          }) : []
        };
      });
    },
    filterHotComment(rawList) {
      return rawList.map((item) => {
        return {
          id: `${item.SeqNo}_${item.CmId}`,
          rootId: item.SeqNo,
          text: item.Content ? this.replaceEmoji(item.Content).replace(/\\n/g, "\n") : "",
          time: item.PubTime ? this.formatTime(item.PubTime) : null,
          timeStr: item.PubTime ? dateFormat2(this.formatTime(item.PubTime)) : null,
          userName: item.Nick ?? "",
          images: item.Pic ? [item.Pic] : [],
          avatar: item.Avatar,
          location: item.Location ? item.Location : "",
          userId: item.EncryptUin,
          likedCount: item.PraiseNum,
          reply: item.SubComments ? item.SubComments.map((c) => {
            return {
              id: `sub_${c.SeqNo}_${c.CmId}`,
              text: this.replaceEmoji(c.Content).replace(/\\n/g, "\n"),
              time: c.PubTime ? this.formatTime(c.PubTime) : null,
              timeStr: c.PubTime ? dateFormat2(this.formatTime(c.PubTime)) : null,
              userName: c.Nick ?? "",
              avatar: c.Avatar,
              images: c.Pic ? [c.Pic] : [],
              userId: c.EncryptUin,
              likedCount: c.PraiseNum
            };
          }) : []
        };
      });
    },
    replaceEmoji(msg) {
      let rxp = /^\[em\](e\d+)\[\/em\]$/;
      let result = msg.match(/\[em\]e\d+\[\/em\]/g);
      if (!result) return msg;
      result = Array.from(new Set(result));
      for (let item of result) {
        let code = item.replace(rxp, "$1");
        msg = msg.replace(new RegExp(item.replace("[em]", "\\[em\\]").replace("[/em]", "\\[\\/em\\]"), "g"), emojis[code] || "");
      }
      return msg;
    },
    formatTime(time) {
      return String(time).length < 10 ? null : parseInt(time + "000");
    }
  };

  // src/utils/musicSdk/tx/index.js
  var tx = {
    // tipSearch,
    leaderboard: leaderboard_default3,
    songList: songList_default3,
    musicSearch: musicSearch_default3,
    hotSearch: hotSearch_default3,
    comment: comment_default3,
    getMusicUrl(songInfo, type) {
      return apis("tx").getMusicUrl(songInfo, type);
    },
    getLyric(songInfo) {
      return lyric_default3.getLyric(songInfo);
    },
    async getPic(songInfo) {
      return `https://y.gtimg.cn/music/photo_new/T002R500x500M000${songInfo.albumId}.jpg`;
    },
    getMusicDetailPageUrl(songInfo) {
      return `https://y.qq.com/n/yqq/song/${songInfo.songmid}.html`;
    }
  };
  var tx_default = tx;

  // src/utils/musicSdk/wy/utils/crypto.js
  var iv = btoa("0102030405060708");
  var presetKey = btoa("0CoJUm6Qyw8W8jud");
  var linuxapiKey = btoa("rFgB&h#%2?^eDg:Q");
  var publicKey = "-----BEGIN PUBLIC KEY-----\nMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDgtQn2JZ34ZC28NWYpAUd98iZ37BUrX/aKzmFbt7clFSs6sXqHauqKWqdtLkF2KexO40H1YTX8z2lSgBBOAxLsvaklV8k4cBFK9snQXE9/DDaFt6Rr7iVZMldczhC0JNgTz+SHXT6CBHuX3e9SdB1Ua44oncaTWz7OBGLbCiK45wIDAQAB\n-----END PUBLIC KEY-----";
  var eapiKey = btoa("e82ckenh8dichen8");
  var aesEncrypt = (b64, mode, key, iv2) => {
    return aesEncryptSync(b64, key, iv2, mode);
  };
  var rsaEncrypt = (buffer, key) => {
    buffer = Buffer.concat([Buffer.alloc(128 - buffer.length), buffer]);
    return Buffer.from(rsaEncryptSync(buffer.toString("base64"), key, RSA_PADDING.NoPadding), "base64");
  };
  var weapi = (object) => {
    const text = JSON.stringify(object);
    const secretKey = String(Math.random()).substring(2, 18);
    return {
      params: aesEncrypt(btoa(aesEncrypt(Buffer.from(text).toString("base64"), AES_MODE.CBC_128_PKCS7Padding, presetKey, iv)), AES_MODE.CBC_128_PKCS7Padding, btoa(secretKey), iv),
      encSecKey: rsaEncrypt(Buffer.from(secretKey).reverse(), publicKey).toString("hex")
    };
  };
  var linuxapi = (object) => {
    const text = JSON.stringify(object);
    return {
      eparams: Buffer.from(aesEncrypt(Buffer.from(text).toString("base64"), AES_MODE.ECB_128_NoPadding, linuxapiKey, ""), "base64").toString("hex").toUpperCase()
    };
  };
  var eapi = (url, object) => {
    const text = typeof object === "object" ? JSON.stringify(object) : object;
    const message = `nobody${url}use${text}md5forencrypt`;
    const digest = toMD5(message);
    const data = `${url}-36cd479b6b5-${text}-36cd479b6b5-${digest}`;
    return {
      params: Buffer.from(aesEncrypt(Buffer.from(data).toString("base64"), AES_MODE.ECB_128_NoPadding, eapiKey, ""), "base64").toString("hex").toUpperCase()
    };
  };

  // src/utils/musicSdk/wy/musicDetail.js
  var musicDetail_default = {
    getSinger(singers) {
      let arr = [];
      singers?.forEach((singer) => {
        arr.push(singer.name);
      });
      return arr.join("\u3001");
    },
    filterList({ songs, privileges }) {
      const list = [];
      songs.forEach((item, index) => {
        const types = [];
        const _types = {};
        let size;
        let privilege = privileges[index];
        if (privilege.id !== item.id) privilege = privileges.find((p) => p.id === item.id);
        if (!privilege) return;
        if (privilege.maxBrLevel == "hires") {
          size = item.hr ? sizeFormate(item.hr.size) : null;
          types.push({ type: "flac24bit", size });
          _types.flac24bit = {
            size
          };
        }
        switch (privilege.maxbr) {
          case 999e3:
            size = item.sq ? sizeFormate(item.sq.size) : null;
            types.push({ type: "flac", size });
            _types.flac = {
              size
            };
          case 32e4:
            size = item.h ? sizeFormate(item.h.size) : null;
            types.push({ type: "320k", size });
            _types["320k"] = {
              size
            };
          case 192e3:
          case 128e3:
            size = item.l ? sizeFormate(item.l.size) : null;
            types.push({ type: "128k", size });
            _types["128k"] = {
              size
            };
        }
        types.reverse();
        if (item.pc) {
          list.push({
            singer: item.pc.ar ?? "",
            name: item.pc.sn ?? "",
            albumName: item.pc.alb ?? "",
            albumId: item.al?.id,
            source: "wy",
            interval: formatPlayTime(item.dt / 1e3),
            songmid: item.id,
            img: item.al?.picUrl ?? "",
            lrc: null,
            otherSource: null,
            types,
            _types,
            typeUrl: {}
          });
        } else {
          list.push({
            singer: this.getSinger(item.ar),
            name: item.name ?? "",
            albumName: item.al?.name,
            albumId: item.al?.id,
            source: "wy",
            interval: formatPlayTime(item.dt / 1e3),
            songmid: item.id,
            img: item.al?.picUrl,
            lrc: null,
            otherSource: null,
            types,
            _types,
            typeUrl: {}
          });
        }
      });
      return list;
    },
    async getList(ids = [], retryNum = 0) {
      if (retryNum > 2) return Promise.reject(new Error("try max num"));
      const requestObj = httpFetch("https://music.163.com/weapi/v3/song/detail", {
        method: "post",
        headers: {
          "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36",
          origin: "https://music.163.com"
        },
        form: weapi({
          c: "[" + ids.map((id) => '{"id":' + id + "}").join(",") + "]",
          ids: "[" + ids.join(",") + "]"
        })
      });
      const { body, statusCode } = await requestObj.promise;
      if (statusCode != 200 || body.code !== 200) throw new Error("\u83B7\u53D6\u6B4C\u66F2\u8BE6\u60C5\u5931\u8D25");
      return { source: "wy", list: this.filterList(body) };
    }
  };

  // src/utils/musicSdk/wy/leaderboard.js
  var topList = [
    { id: "wy__19723756", name: "\u98D9\u5347\u699C", bangid: "19723756" },
    { id: "wy__3779629", name: "\u65B0\u6B4C\u699C", bangid: "3779629" },
    { id: "wy__2884035", name: "\u539F\u521B\u699C", bangid: "2884035" },
    { id: "wy__3778678", name: "\u70ED\u6B4C\u699C", bangid: "3778678" },
    { id: "wy__991319590", name: "\u8BF4\u5531\u699C", bangid: "991319590" },
    { id: "wy__71384707", name: "\u53E4\u5178\u699C", bangid: "71384707" },
    { id: "wy__1978921795", name: "\u7535\u97F3\u699C", bangid: "1978921795" },
    { id: "wy__5453912201", name: "\u9ED1\u80F6VIP\u7231\u542C\u699C", bangid: "5453912201" },
    { id: "wy__71385702", name: "ACG\u699C", bangid: "71385702" },
    { id: "wy__745956260", name: "\u97E9\u8BED\u699C", bangid: "745956260" },
    { id: "wy__10520166", name: "\u56FD\u7535\u699C", bangid: "10520166" },
    { id: "wy__180106", name: "UK\u6392\u884C\u699C\u5468\u699C", bangid: "180106" },
    { id: "wy__60198", name: "\u7F8E\u56FDBillboard\u699C", bangid: "60198" },
    { id: "wy__3812895", name: "Beatport\u5168\u7403\u7535\u5B50\u821E\u66F2\u699C", bangid: "3812895" },
    { id: "wy__21845217", name: "KTV\u551B\u699C", bangid: "21845217" },
    { id: "wy__60131", name: "\u65E5\u672COricon\u699C", bangid: "60131" },
    { id: "wy__2809513713", name: "\u6B27\u7F8E\u70ED\u6B4C\u699C", bangid: "2809513713" },
    { id: "wy__2809577409", name: "\u6B27\u7F8E\u65B0\u6B4C\u699C", bangid: "2809577409" },
    { id: "wy__27135204", name: "\u6CD5\u56FD NRJ Vos Hits \u5468\u699C", bangid: "27135204" },
    { id: "wy__3001835560", name: "ACG\u52A8\u753B\u699C", bangid: "3001835560" },
    { id: "wy__3001795926", name: "ACG\u6E38\u620F\u699C", bangid: "3001795926" },
    { id: "wy__3001890046", name: "ACG VOCALOID\u699C", bangid: "3001890046" },
    { id: "wy__3112516681", name: "\u4E2D\u56FD\u65B0\u4E61\u6751\u97F3\u4E50\u6392\u884C\u699C", bangid: "3112516681" },
    { id: "wy__5059644681", name: "\u65E5\u8BED\u699C", bangid: "5059644681" },
    { id: "wy__5059633707", name: "\u6447\u6EDA\u699C", bangid: "5059633707" },
    { id: "wy__5059642708", name: "\u56FD\u98CE\u699C", bangid: "5059642708" },
    { id: "wy__5338990334", name: "\u6F5C\u529B\u7206\u6B3E\u699C", bangid: "5338990334" },
    { id: "wy__5059661515", name: "\u6C11\u8C23\u699C", bangid: "5059661515" },
    { id: "wy__6688069460", name: "\u542C\u6B4C\u8BC6\u66F2\u699C", bangid: "6688069460" },
    { id: "wy__6723173524", name: "\u7F51\u7EDC\u70ED\u6B4C\u699C", bangid: "6723173524" },
    { id: "wy__6732051320", name: "\u4FC4\u8BED\u699C", bangid: "6732051320" },
    { id: "wy__6732014811", name: "\u8D8A\u5357\u8BED\u699C", bangid: "6732014811" },
    { id: "wy__6886768100", name: "\u4E2D\u6587DJ\u699C", bangid: "6886768100" },
    { id: "wy__6939992364", name: "\u4FC4\u7F57\u65AFtop hit\u6D41\u884C\u97F3\u4E50\u699C", bangid: "6939992364" },
    { id: "wy__7095271308", name: "\u6CF0\u8BED\u699C", bangid: "7095271308" },
    { id: "wy__7356827205", name: "BEAT\u6392\u884C\u699C", bangid: "7356827205" },
    { id: "wy__7325478166", name: "\u7F16\u8F91\u63A8\u8350\u699CVOL.44 \u5929\u624D\u5973\u5B50\u6447\u6EDA\u4E50\u961Fboygenius\u5256\u767D\u5351\u5FAE\u5FC3\u8FF9", bangid: "7325478166" },
    { id: "wy__7603212484", name: "LOOK\u76F4\u64AD\u6B4C\u66F2\u699C", bangid: "7603212484" },
    { id: "wy__7775163417", name: "\u8D4F\u97F3\u699C", bangid: "7775163417" },
    { id: "wy__7785123708", name: "\u9ED1\u80F6VIP\u65B0\u6B4C\u699C", bangid: "7785123708" },
    { id: "wy__7785066739", name: "\u9ED1\u80F6VIP\u70ED\u6B4C\u699C", bangid: "7785066739" },
    { id: "wy__7785091694", name: "\u9ED1\u80F6VIP\u7231\u641C\u699C", bangid: "7785091694" }
  ];
  var leaderboard_default4 = {
    limit: 1e5,
    list: [
      {
        id: "wybsb",
        name: "\u98D9\u5347\u699C",
        bangid: "19723756"
      },
      {
        id: "wyrgb",
        name: "\u70ED\u6B4C\u699C",
        bangid: "3778678"
      },
      {
        id: "wyxgb",
        name: "\u65B0\u6B4C\u699C",
        bangid: "3779629"
      },
      {
        id: "wyycb",
        name: "\u539F\u521B\u699C",
        bangid: "2884035"
      },
      {
        id: "wygdb",
        name: "\u53E4\u5178\u699C",
        bangid: "71384707"
      },
      {
        id: "wydouyb",
        name: "\u6296\u97F3\u699C",
        bangid: "2250011882"
      },
      {
        id: "wyhyb",
        name: "\u97E9\u8BED\u699C",
        bangid: "745956260"
      },
      {
        id: "wydianyb",
        name: "\u7535\u97F3\u699C",
        bangid: "1978921795"
      },
      {
        id: "wydjb",
        name: "\u7535\u7ADE\u699C",
        bangid: "2006508653"
      },
      {
        id: "wyktvbb",
        name: "KTV\u551B\u699C",
        bangid: "21845217"
      }
    ],
    getUrl(id) {
      return `https://music.163.com/discover/toplist?id=${id}`;
    },
    regExps: {
      list: /<textarea id="song-list-pre-data" style="display:none;">(.+?)<\/textarea>/
    },
    _requestBoardsObj: null,
    getBoardsData() {
      if (this._requestBoardsObj) this._requestBoardsObj.cancelHttp();
      this._requestBoardsObj = httpFetch("https://music.163.com/weapi/toplist", {
        method: "post",
        form: weapi({})
      });
      return this._requestBoardsObj.promise;
    },
    getData(id) {
      const requestBoardsDetailObj = httpFetch("https://music.163.com/weapi/v3/playlist/detail", {
        method: "post",
        form: weapi({
          id,
          n: 1e5,
          p: 1
        })
      });
      return requestBoardsDetailObj.promise;
    },
    filterBoardsData(rawList) {
      let list = [];
      for (const board of rawList) {
        list.push({
          id: "wy__" + board.id,
          name: board.name,
          bangid: String(board.id)
        });
      }
      return list;
    },
    async getBoards(retryNum = 0) {
      this.list = topList;
      return {
        list: topList,
        source: "wy"
      };
    },
    async getList(bangid, page, retryNum = 0) {
      if (++retryNum > 6) return Promise.reject(new Error("try max num"));
      let resp;
      try {
        resp = await this.getData(bangid);
      } catch (err) {
        if (err.message == "try max num") {
          throw err;
        } else {
          return this.getList(bangid, page, retryNum);
        }
      }
      if (resp.statusCode !== 200 || resp.body.code !== 200) return this.getList(bangid, page, retryNum);
      let musicDetail;
      try {
        musicDetail = await musicDetail_default.getList(resp.body.playlist.trackIds.map((trackId) => trackId.id));
      } catch (err) {
        console.log(err);
        if (err.message == "try max num") {
          throw err;
        } else {
          return this.getList(bangid, page, retryNum);
        }
      }
      return {
        total: musicDetail.list.length,
        list: musicDetail.list,
        limit: this.limit,
        page,
        source: "wy"
      };
    },
    getDetailPageUrl(id) {
      if (typeof id == "string") id = id.replace("wy__", "");
      return `https://music.163.com/#/discover/toplist?id=${id}`;
    }
  };

  // src/utils/musicSdk/wy/lyric.js
  var eapiRequest = (url, data) => {
    return httpFetch("https://interface3.music.163.com/eapi/song/lyric/v1", {
      method: "post",
      headers: {
        "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36",
        origin: "https://music.163.com"
        // cookie: 'os=pc; deviceId=A9C064BB4584D038B1565B58CB05F95290998EE8B025AA2D07AE; osver=Microsoft-Windows-10-Home-China-build-19043-64bit; appver=2.5.2.197409; channel=netease; MUSIC_A=37a11f2eb9de9930cad479b2ad495b0e4c982367fb6f909d9a3f18f876c6b49faddb3081250c4980dd7e19d4bd9bf384e004602712cf2b2b8efaafaab164268a00b47359f85f22705cc95cb6180f3aee40f5be1ebf3148d888aa2d90636647d0c3061cd18d77b7a0; __csrf=05b50d54082694f945d7de75c210ef94; mode=Z7M-KP5(7)GZ; NMTID=00OZLp2VVgq9QdwokUgq3XNfOddQyIAAAF_6i8eJg; ntes_kaola_ad=1',
      },
      form: eapi(url, data)
    });
  };
  var parseTools2 = {
    rxps: {
      info: /^{"/,
      lineTime: /^\[(\d+),\d+\]/,
      wordTime: /\(\d+,\d+,\d+\)/,
      wordTimeAll: /(\(\d+,\d+,\d+\))/g,
      timeMs2: /\[\d+:\d+\.\d{2}]/,
      timeMs3: /\[\d+:\d+\.\d{3}]/
    },
    msFormat(timeMs, pad3 = true) {
      if (Number.isNaN(timeMs)) return "";
      let ms = (timeMs % 1e3).toString().padStart(pad3 ? 3 : 2, "0");
      if (!pad3 && ms.length > 2) ms = ms.slice(0, 2);
      timeMs /= 1e3;
      let m = parseInt(timeMs / 60).toString().padStart(2, "0");
      timeMs %= 60;
      let s = parseInt(timeMs).toString().padStart(2, "0");
      return `[${m}:${s}.${ms}]`;
    },
    parseLyric(lines) {
      const lxlrcLines = [];
      const lrcLines = [];
      for (let line of lines) {
        line = line.trim();
        let result = this.rxps.lineTime.exec(line);
        if (!result) {
          if (line.startsWith("[offset")) {
            lxlrcLines.push(line);
            lrcLines.push(line);
          }
          continue;
        }
        const startMsTime = parseInt(result[1]);
        const startTimeStr = this.msFormat(startMsTime);
        if (!startTimeStr) continue;
        let words = line.replace(this.rxps.lineTime, "");
        lrcLines.push(`${startTimeStr}${words.replace(this.rxps.wordTimeAll, "")}`);
        let times = words.match(this.rxps.wordTimeAll);
        if (!times) continue;
        times = times.map((time) => {
          const result2 = /\((\d+),(\d+),\d+\)/.exec(time);
          return `<${Math.trunc(Math.max(parseInt(result2[1]) - startMsTime, 0))},${result2[2]}>`;
        });
        const wordArr = words.split(this.rxps.wordTime);
        wordArr.shift();
        const newWords = times.map((time, index) => `${time}${wordArr[index]}`).join("");
        lxlrcLines.push(`${startTimeStr}${newWords}`);
      }
      return {
        lyric: lrcLines.join("\n"),
        lxlyric: lxlrcLines.join("\n")
      };
    },
    parseHeaderInfo(str) {
      str = str.trim();
      str = str.replace(/\r/g, "");
      if (!str) return null;
      const isPad3 = this.rxps.timeMs3.test(str) || !this.rxps.timeMs2.test(str);
      const lines = str.split("\n");
      return lines.map((line) => {
        if (!this.rxps.info.test(line)) return line;
        try {
          const info = JSON.parse(line);
          const timeTag = this.msFormat(info.t, isPad3);
          return timeTag ? `${timeTag}${info.c.map((t) => t.tx).join("")}` : "";
        } catch {
          return "";
        }
      });
    },
    getIntv(interval) {
      if (!interval) return 0;
      if (!interval.includes(".")) interval += ".0";
      let arr = interval.split(/:|\./);
      while (arr.length < 3) arr.unshift("0");
      const [m, s, ms] = arr;
      return parseInt(m) * 36e5 + parseInt(s) * 1e3 + parseInt(ms);
    },
    fixTimeTag(lrc, targetlrc) {
      let lrcLines = lrc.split("\n");
      const targetlrcLines = targetlrc.split("\n");
      const timeRxp = /^\[([\d:.]+)\]/;
      let temp = [];
      let newLrc = [];
      targetlrcLines.forEach((line) => {
        const result = timeRxp.exec(line);
        if (!result) return;
        const words = line.replace(timeRxp, "");
        if (!words.trim()) return;
        const t1 = this.getIntv(result[1]);
        while (lrcLines.length) {
          const lrcLine = lrcLines.shift();
          const lrcLineResult = timeRxp.exec(lrcLine);
          if (!lrcLineResult) continue;
          const t2 = this.getIntv(lrcLineResult[1]);
          if (Math.abs(t1 - t2) < 100) {
            const lrc2 = line.replace(timeRxp, lrcLineResult[0]).trim();
            if (!lrc2) continue;
            newLrc.push(lrc2);
            break;
          }
          temp.push(lrcLine);
        }
        lrcLines = [...temp, ...lrcLines];
        temp = [];
      });
      return newLrc.join("\n");
    },
    parse(ylrc, ytlrc, yrlrc, lrc, tlrc, rlrc) {
      const info = {
        lyric: "",
        tlyric: "",
        rlyric: "",
        lxlyric: ""
      };
      if (ylrc) {
        let lines = this.parseHeaderInfo(ylrc);
        if (lines) {
          const result = this.parseLyric(lines);
          if (ytlrc) {
            const lines2 = this.parseHeaderInfo(ytlrc);
            if (lines2) {
              info.tlyric = this.fixTimeTag(result.lyric, lines2.join("\n"));
            }
          }
          if (yrlrc) {
            const lines2 = this.parseHeaderInfo(yrlrc);
            if (lines2) {
              info.rlyric = this.fixTimeTag(result.lyric, lines2.join("\n"));
            }
          }
          const timeRxp = /^\[[\d:.]+\]/;
          const headers2 = lines.filter((l) => timeRxp.test(l)).join("\n");
          info.lyric = `${headers2}
${result.lyric}`;
          info.lxlyric = result.lxlyric;
          return info;
        }
      }
      if (lrc) {
        const lines = this.parseHeaderInfo(lrc);
        if (lines) info.lyric = lines.join("\n");
      }
      if (tlrc) {
        const lines = this.parseHeaderInfo(tlrc);
        if (lines) info.tlyric = lines.join("\n");
      }
      if (rlrc) {
        const lines = this.parseHeaderInfo(rlrc);
        if (lines) info.rlyric = lines.join("\n");
      }
      return info;
    }
  };
  var fixTimeLabel = (lrc, tlrc, romalrc) => {
    if (lrc) {
      let newLrc = lrc.replace(/\[(\d{2}:\d{2}):(\d{2})]/g, "[$1.$2]");
      let newTlrc = tlrc?.replace(/\[(\d{2}:\d{2}):(\d{2})]/g, "[$1.$2]") ?? tlrc;
      if (newLrc != lrc || newTlrc != tlrc) {
        lrc = newLrc;
        tlrc = newTlrc;
        if (romalrc) romalrc = romalrc.replace(/\[(\d{2}:\d{2}):(\d{2,3})]/g, "[$1.$2]").replace(/\[(\d{2}:\d{2}\.\d{2})0]/g, "[$1]");
      }
    }
    return { lrc, tlrc, romalrc };
  };
  var lyric_default4 = (songmid) => {
    const requestObj = eapiRequest("/api/song/lyric/v1", {
      id: songmid,
      cp: false,
      tv: 0,
      lv: 0,
      rv: 0,
      kv: 0,
      yv: 0,
      ytv: 0,
      yrv: 0
    });
    requestObj.promise = requestObj.promise.then(({ body }) => {
      if (body.code !== 200 || !body?.lrc?.lyric) return Promise.reject(new Error("Get lyric failed"));
      const fixTimeLabelLrc = fixTimeLabel(body.lrc.lyric, body.tlyric?.lyric, body.romalrc?.lyric);
      const info = parseTools2.parse(body.yrc?.lyric, body.ytlrc?.lyric, body.yromalrc?.lyric, fixTimeLabelLrc.lrc, fixTimeLabelLrc.tlrc, fixTimeLabelLrc.romalrc);
      if (!info.lyric) return Promise.reject(new Error("Get lyric failed"));
      return info;
    });
    return requestObj;
  };

  // src/utils/musicSdk/wy/musicInfo.js
  var musicInfo_default2 = (songmid) => {
    const requestObj = httpFetch("https://music.163.com/weapi/v3/song/detail", {
      method: "post",
      headers: {
        "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36",
        Referer: "https://music.163.com/song?id=" + songmid,
        origin: "https://music.163.com"
      },
      form: weapi({
        c: `[{"id":${songmid}}]`,
        ids: `[${songmid}]`
      })
    });
    requestObj.promise = requestObj.promise.then(({ body }) => {
      if (body.code !== 200 || !body.songs.length) return Promise.reject(new Error("\u83B7\u53D6\u6B4C\u66F2\u4FE1\u606F\u5931\u8D25"));
      return body.songs[0];
    });
    return requestObj;
  };

  // src/utils/musicSdk/wy/utils/index.js
  var eapiRequest2 = (url, data) => {
    return httpFetch("http://interface.music.163.com/eapi/batch", {
      method: "post",
      headers: {
        "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36",
        origin: "https://music.163.com"
        // cookie: 'os=pc; deviceId=A9C064BB4584D038B1565B58CB05F95290998EE8B025AA2D07AE; osver=Microsoft-Windows-10-Home-China-build-19043-64bit; appver=2.5.2.197409; channel=netease; MUSIC_A=37a11f2eb9de9930cad479b2ad495b0e4c982367fb6f909d9a3f18f876c6b49faddb3081250c4980dd7e19d4bd9bf384e004602712cf2b2b8efaafaab164268a00b47359f85f22705cc95cb6180f3aee40f5be1ebf3148d888aa2d90636647d0c3061cd18d77b7a0; __csrf=05b50d54082694f945d7de75c210ef94; mode=Z7M-KP5(7)GZ; NMTID=00OZLp2VVgq9QdwokUgq3XNfOddQyIAAAF_6i8eJg; ntes_kaola_ad=1',
      },
      form: eapi(url, data)
    });
  };

  // src/utils/musicSdk/wy/musicSearch.js
  var musicSearch_default4 = {
    limit: 30,
    total: 0,
    page: 0,
    allPage: 1,
    musicSearch(str, page, limit) {
      const searchRequest = eapiRequest2("/api/search/song/list/page", {
        keyword: str,
        needCorrect: "1",
        channel: "typing",
        offset: limit * (page - 1),
        scene: "normal",
        total: page == 1,
        limit
      });
      return searchRequest.promise.then(({ body }) => body);
    },
    getSinger(singers) {
      let arr = [];
      singers.forEach((singer) => {
        arr.push(singer.name);
      });
      return arr.join("\u3001");
    },
    handleResult(rawList) {
      if (!rawList) return [];
      return rawList.map((item) => {
        item = item.baseInfo.simpleSongData;
        const types = [];
        const _types = {};
        let size;
        if (item.privilege.maxBrLevel == "hires") {
          size = item.hr ? sizeFormate(item.hr.size) : null;
          types.push({ type: "flac24bit", size });
          _types.flac24bit = {
            size
          };
        }
        switch (item.privilege.maxbr) {
          case 999e3:
            size = item.sq ? sizeFormate(item.sq.size) : null;
            types.push({ type: "flac", size });
            _types.flac = {
              size
            };
          case 32e4:
            size = item.h ? sizeFormate(item.h.size) : null;
            types.push({ type: "320k", size });
            _types["320k"] = {
              size
            };
          case 192e3:
          case 128e3:
            size = item.l ? sizeFormate(item.l.size) : null;
            types.push({ type: "128k", size });
            _types["128k"] = {
              size
            };
        }
        types.reverse();
        return {
          singer: this.getSinger(item.ar),
          name: item.name,
          albumName: item.al.name,
          albumId: item.al.id,
          source: "wy",
          interval: formatPlayTime(item.dt / 1e3),
          songmid: item.id,
          img: item.al.picUrl,
          lrc: null,
          types,
          _types,
          typeUrl: {}
        };
      });
    },
    search(str, page = 1, limit, retryNum = 0) {
      if (++retryNum > 3) return Promise.reject(new Error("try max num"));
      if (limit == null) limit = this.limit;
      return this.musicSearch(str, page, limit).then((result) => {
        if (!result || result.code !== 200) return this.search(str, page, limit, retryNum);
        let list = this.handleResult(result.data.resources || []);
        if (list == null) return this.search(str, page, limit, retryNum);
        this.total = result.data.totalCount || 0;
        this.page = page;
        this.allPage = Math.ceil(this.total / this.limit);
        return {
          list,
          allPage: this.allPage,
          limit: this.limit,
          total: this.total,
          source: "wy"
        };
      });
    }
  };

  // src/utils/musicSdk/wy/songList.js
  var songList_default4 = {
    _requestObj_tags: null,
    _requestObj_hotTags: null,
    _requestObj_list: null,
    limit_list: 30,
    limit_song: 1e5,
    successCode: 200,
    cookie: "MUSIC_U=",
    sortList: [
      {
        name: "\u6700\u70ED",
        tid: "hot",
        id: "hot"
      }
      // {
      //   name: '最新',
      //   tid: 'new',
      //   id: 'new',
      // },
    ],
    regExps: {
      listDetailLink: /^.+(?:\?|&)id=(\d+)(?:&.*$|#.*$|$)/,
      listDetailLink2: /^.+\/playlist\/(\d+)\/\d+\/.+$/
    },
    async handleParseId(link, retryNum = 0) {
      if (retryNum > 2) throw new Error("link try max num");
      const requestObj_listDetailLink = httpFetch(link);
      const { url, statusCode } = await requestObj_listDetailLink.promise;
      if (statusCode > 400) return this.handleParseId(link, ++retryNum);
      return this.regExps.listDetailLink.test(url) ? url.replace(this.regExps.listDetailLink, "$1") : url.replace(this.regExps.listDetailLink2, "$1");
    },
    async getListId(id) {
      let cookie;
      if (/###/.test(id)) {
        const [url, token] = id.split("###");
        id = url;
        cookie = `MUSIC_U=${token}`;
      }
      if (/[?&:/]/.test(id)) {
        if (this.regExps.listDetailLink.test(id)) {
          id = id.replace(this.regExps.listDetailLink, "$1");
        } else if (this.regExps.listDetailLink2.test(id)) {
          id = id.replace(this.regExps.listDetailLink2, "$1");
        } else {
          id = await this.handleParseId(id);
        }
      }
      return { id, cookie };
    },
    async getListDetail(rawId, page, tryNum = 0) {
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      const { id, cookie } = await this.getListId(rawId);
      if (cookie) this.cookie = cookie;
      const requestObj_listDetail = httpFetch("https://music.163.com/api/linux/forward", {
        method: "post",
        headers: {
          "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36",
          Cookie: this.cookie
        },
        credentials: "omit",
        cache: "default",
        form: linuxapi({
          method: "POST",
          url: "https://music.163.com/api/v3/playlist/detail",
          params: {
            id,
            n: this.limit_song,
            s: 8
          }
        })
      });
      const { statusCode, body } = await requestObj_listDetail.promise;
      if (statusCode !== 200 || body.code !== this.successCode) return this.getListDetail(id, page, ++tryNum);
      let limit = 1e3;
      let rangeStart = (page - 1) * limit;
      let list;
      if (body.playlist.trackIds.length == body.privileges.length) {
        list = this.filterListDetail(body);
      } else {
        try {
          list = (await musicDetail_default.getList(body.playlist.trackIds.slice(rangeStart, limit * page).map((trackId) => trackId.id))).list;
        } catch (err) {
          console.log(err);
          if (err.message == "try max num") {
            throw err;
          } else {
            return this.getListDetail(id, page, ++tryNum);
          }
        }
      }
      return {
        list,
        page,
        limit,
        total: body.playlist.trackIds.length,
        source: "wy",
        info: {
          play_count: formatPlayCount(body.playlist.playCount),
          name: body.playlist.name,
          img: body.playlist.coverImgUrl,
          desc: body.playlist.description,
          author: body.playlist.creator.nickname
        }
      };
    },
    filterListDetail({ playlist: { tracks }, privileges }) {
      const list = [];
      tracks.forEach((item, index) => {
        const types = [];
        const _types = {};
        let size;
        let privilege = privileges[index];
        if (privilege.id !== item.id) privilege = privileges.find((p) => p.id === item.id);
        if (!privilege) return;
        if (privilege.maxBrLevel == "hires") {
          size = item.hr ? sizeFormate(item.hr.size) : null;
          types.push({ type: "flac24bit", size });
          _types.flac24bit = {
            size
          };
        }
        switch (privilege.maxbr) {
          case 999e3:
            size = null;
            types.push({ type: "flac", size });
            _types.flac = {
              size
            };
          case 32e4:
            size = item.h ? sizeFormate(item.h.size) : null;
            types.push({ type: "320k", size });
            _types["320k"] = {
              size
            };
          case 192e3:
          case 128e3:
            size = item.l ? sizeFormate(item.l.size) : null;
            types.push({ type: "128k", size });
            _types["128k"] = {
              size
            };
        }
        types.reverse();
        if (item.pc) {
          list.push({
            singer: item.pc.ar ?? "",
            name: item.pc.sn ?? "",
            albumName: item.pc.alb ?? "",
            albumId: item.al?.id,
            source: "wy",
            interval: formatPlayTime(item.dt / 1e3),
            songmid: item.id,
            img: item.al?.picUrl ?? "",
            lrc: null,
            otherSource: null,
            types,
            _types,
            typeUrl: {}
          });
        } else {
          list.push({
            singer: formatSingerName(item.ar, "name"),
            name: item.name ?? "",
            albumName: item.al?.name,
            albumId: item.al?.id,
            source: "wy",
            interval: formatPlayTime(item.dt / 1e3),
            songmid: item.id,
            img: item.al?.picUrl,
            lrc: null,
            otherSource: null,
            types,
            _types,
            typeUrl: {}
          });
        }
      });
      return list;
    },
    // 获取列表数据
    getList(sortId, tagId, page, tryNum = 0) {
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      if (this._requestObj_list) this._requestObj_list.cancelHttp();
      this._requestObj_list = httpFetch("https://music.163.com/weapi/playlist/list", {
        method: "post",
        form: weapi({
          cat: tagId || "\u5168\u90E8",
          // 全部,华语,欧美,日语,韩语,粤语,小语种,流行,摇滚,民谣,电子,舞曲,说唱,轻音乐,爵士,乡村,R&B/Soul,古典,民族,英伦,金属,朋克,蓝调,雷鬼,世界音乐,拉丁,另类/独立,New Age,古风,后摇,Bossa Nova,清晨,夜晚,学习,工作,午休,下午茶,地铁,驾车,运动,旅行,散步,酒吧,怀旧,清新,浪漫,性感,伤感,治愈,放松,孤独,感动,兴奋,快乐,安静,思念,影视原声,ACG,儿童,校园,游戏,70后,80后,90后,网络歌曲,KTV,经典,翻唱,吉他,钢琴,器乐,榜单,00后
          order: sortId,
          // hot,new
          limit: this.limit_list,
          offset: this.limit_list * (page - 1),
          total: true
        })
      });
      return this._requestObj_list.promise.then(({ body }) => {
        if (body.code !== this.successCode) return this.getList(sortId, tagId, page, ++tryNum);
        return {
          list: this.filterList(body.playlists),
          total: parseInt(body.total),
          page,
          limit: this.limit_list,
          source: "wy"
        };
      });
    },
    filterList(rawData) {
      return rawData.map((item) => ({
        play_count: formatPlayCount(item.playCount),
        id: String(item.id),
        author: item.creator.nickname,
        name: item.name,
        time: item.createTime ? dateFormat(item.createTime, "Y-M-D") : "",
        img: item.coverImgUrl,
        grade: item.grade,
        total: item.trackCount,
        desc: item.description,
        source: "wy"
      }));
    },
    // 获取标签
    getTag(tryNum = 0) {
      if (this._requestObj_tags) this._requestObj_tags.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      this._requestObj_tags = httpFetch("https://music.163.com/weapi/playlist/catalogue", {
        method: "post",
        form: weapi({})
      });
      return this._requestObj_tags.promise.then(({ body }) => {
        if (body.code !== this.successCode) return this.getTag(++tryNum);
        return this.filterTagInfo(body);
      });
    },
    filterTagInfo({ sub, categories }) {
      const subList = {};
      for (const item of sub) {
        if (!subList[item.category]) subList[item.category] = [];
        subList[item.category].push({
          parent_id: categories[item.category],
          parent_name: categories[item.category],
          id: item.name,
          name: item.name,
          source: "wy"
        });
      }
      const list = [];
      for (const key of Object.keys(categories)) {
        list.push({
          name: categories[key],
          list: subList[key],
          source: "wy"
        });
      }
      return list;
    },
    // 获取热门标签
    getHotTag(tryNum = 0) {
      if (this._requestObj_hotTags) this._requestObj_hotTags.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      this._requestObj_hotTags = httpFetch("https://music.163.com/weapi/playlist/hottags", {
        method: "post",
        form: weapi({})
      });
      return this._requestObj_hotTags.promise.then(({ body }) => {
        if (body.code !== this.successCode) return this.getTag(++tryNum);
        return this.filterHotTagInfo(body.tags);
      });
    },
    filterHotTagInfo(rawList) {
      return rawList.map((item) => ({
        id: item.playlistTag.name,
        name: item.playlistTag.name,
        source: "wy"
      }));
    },
    getTags() {
      return Promise.all([this.getTag(), this.getHotTag()]).then(([tags, hotTag]) => ({ tags, hotTag, source: "wy" }));
    },
    async getDetailPageUrl(rawId) {
      const { id } = await this.getListId(rawId);
      return `https://music.163.com/#/playlist?id=${id}`;
    },
    search(text, page, limit = 20) {
      return eapiRequest2("/api/cloudsearch/pc", {
        s: text,
        type: 1e3,
        // 1: 单曲, 10: 专辑, 100: 歌手, 1000: 歌单, 1002: 用户, 1004: MV, 1006: 歌词, 1009: 电台, 1014: 视频
        limit,
        total: page == 1,
        offset: limit * (page - 1)
      }).promise.then(({ body }) => {
        if (body.code != this.successCode) throw new Error("filed");
        return {
          list: this.filterList(body.result.playlists),
          limit,
          total: body.result.playlistCount,
          source: "wy"
        };
      });
    }
  };

  // src/utils/musicSdk/wy/hotSearch.js
  var hotSearch_default4 = {
    _requestObj: null,
    async getList(retryNum = 0) {
      if (this._requestObj) this._requestObj.cancelHttp();
      if (retryNum > 2) return Promise.reject(new Error("try max num"));
      const _requestObj = eapiRequest2("/api/search/chart/detail", {
        id: "HOT_SEARCH_SONG#@#"
      });
      const { body, statusCode } = await _requestObj.promise;
      if (statusCode != 200 || body.code !== 200) throw new Error("\u83B7\u53D6\u70ED\u641C\u8BCD\u5931\u8D25");
      return { source: "wy", list: this.filterList(body.data.itemList) };
    },
    filterList(rawList) {
      return rawList.map((item) => item.searchWord);
    }
  };

  // src/utils/musicSdk/wy/comment.js
  var emojis2 = [
    ["\u5927\u7B11", "\u{1F603}"],
    ["\u53EF\u7231", "\u{1F60A}"],
    ["\u61A8\u7B11", "\u263A\uFE0F"],
    ["\u8272", "\u{1F60D}"],
    ["\u4EB2\u4EB2", "\u{1F619}"],
    ["\u60CA\u6050", "\u{1F631}"],
    ["\u6D41\u6CEA", "\u{1F62D}"],
    ["\u4EB2", "\u{1F61A}"],
    ["\u5446", "\u{1F633}"],
    ["\u54C0\u4F24", "\u{1F614}"],
    ["\u5472\u7259", "\u{1F601}"],
    ["\u5410\u820C", "\u{1F61D}"],
    ["\u6487\u5634", "\u{1F612}"],
    ["\u6012", "\u{1F621}"],
    ["\u5978\u7B11", "\u{1F60F}"],
    ["\u6C57", "\u{1F613}"],
    ["\u75DB\u82E6", "\u{1F616}"],
    ["\u60F6\u6050", "\u{1F630}"],
    ["\u751F\u75C5", "\u{1F628}"],
    ["\u53E3\u7F69", "\u{1F637}"],
    ["\u5927\u54ED", "\u{1F602}"],
    ["\u6655", "\u{1F635}"],
    ["\u53D1\u6012", "\u{1F47F}"],
    ["\u5F00\u5FC3", "\u{1F604}"],
    ["\u9B3C\u8138", "\u{1F61C}"],
    ["\u76B1\u7709", "\u{1F61E}"],
    ["\u6D41\u611F", "\u{1F622}"],
    ["\u7231\u5FC3", "\u2764\uFE0F"],
    ["\u5FC3\u788E", "\u{1F494}"],
    ["\u949F\u60C5", "\u{1F498}"],
    ["\u661F\u661F", "\u2B50\uFE0F"],
    ["\u751F\u6C14", "\u{1F4A2}"],
    ["\u4FBF\u4FBF", "\u{1F4A9}"],
    ["\u5F3A", "\u{1F44D}"],
    ["\u5F31", "\u{1F44E}"],
    ["\u62DC", "\u{1F64F}"],
    ["\u7275\u624B", "\u{1F46B}"],
    ["\u8DF3\u821E", "\u{1F46F}\u200D\u2640\uFE0F"],
    ["\u7981\u6B62", "\u{1F645}\u200D\u2640\uFE0F"],
    ["\u8FD9\u8FB9", "\u{1F481}\u200D\u2640\uFE0F"],
    ["\u7231\u610F", "\u{1F48F}"],
    ["\u793A\u7231", "\u{1F469}\u200D\u2764\uFE0F\u200D\u{1F468}"],
    ["\u5634\u5507", "\u{1F444}"],
    ["\u72D7", "\u{1F436}"],
    ["\u732B", "\u{1F431}"],
    ["\u732A", "\u{1F437}"],
    ["\u5154\u5B50", "\u{1F430}"],
    ["\u5C0F\u9E21", "\u{1F424}"],
    ["\u516C\u9E21", "\u{1F414}"],
    ["\u5E7D\u7075", "\u{1F47B}"],
    ["\u5723\u8BDE", "\u{1F385}"],
    ["\u5916\u661F", "\u{1F47D}"],
    ["\u94BB\u77F3", "\u{1F48E}"],
    ["\u793C\u7269", "\u{1F381}"],
    ["\u7537\u5B69", "\u{1F466}"],
    ["\u5973\u5B69", "\u{1F467}"],
    ["\u86CB\u7CD5", "\u{1F382}"],
    ["18", "\u{1F51E}"],
    ["\u5708", "\u2B55"],
    ["\u53C9", "\u274C"]
  ];
  var applyEmoji = (text) => {
    for (const e of emojis2) text = text.replaceAll(`[${e[0]}]`, e[1]);
    return text;
  };
  var cursorTools = {
    cache: {},
    getCursor(id, page, limit) {
      let cacheData = this.cache[id];
      if (!cacheData) cacheData = this.cache[id] = {};
      let orderType;
      let cursor;
      let offset;
      if (page == 1) {
        cacheData.page = 1;
        cursor = cacheData.cursor = cacheData.prevCursor = Date.now();
        orderType = 1;
        offset = 0;
      } else if (cacheData.page) {
        cursor = cacheData.cursor;
        if (page > cacheData.page) {
          orderType = 1;
          offset = (page - cacheData.page - 1) * limit;
        } else if (page < cacheData.page) {
          orderType = 0;
          offset = (cacheData.page - page - 1) * limit;
        } else {
          cursor = cacheData.cursor = cacheData.prevCursor;
          offset = cacheData.offset;
          orderType = cacheData.orderType;
        }
      }
      return {
        orderType,
        cursor,
        offset
      };
    },
    setCursor(id, cursor, orderType, offset, page) {
      let cacheData = this.cache[id];
      if (!cacheData) cacheData = this.cache[id] = {};
      cacheData.prevCursor = cacheData.cursor;
      cacheData.cursor = cursor;
      cacheData.orderType = orderType;
      cacheData.offset = offset;
      cacheData.page = page;
    }
  };
  var comment_default4 = {
    _requestObj: null,
    _requestObj2: null,
    async getComment({ songmid }, page = 1, limit = 20) {
      if (this._requestObj) this._requestObj.cancelHttp();
      const id = "R_SO_4_" + songmid;
      const cursorInfo = cursorTools.getCursor(songmid, page, limit);
      const _requestObj = httpFetch("https://music.163.com/weapi/comment/resource/comments/get", {
        method: "post",
        headers: {
          "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36",
          origin: "https://music.163.com",
          Refere: "http://music.163.com/"
        },
        form: weapi({
          cursor: cursorInfo.cursor,
          offset: cursorInfo.offset,
          orderType: cursorInfo.orderType,
          pageNo: page,
          pageSize: limit,
          rid: id,
          threadId: id
        })
      });
      const { body, statusCode } = await _requestObj.promise;
      if (statusCode != 200 || body.code !== 200) throw new Error("\u83B7\u53D6\u8BC4\u8BBA\u5931\u8D25");
      cursorTools.setCursor(songmid, body.data.cursor, cursorInfo.orderType, cursorInfo.offset, page);
      return { source: "wy", comments: this.filterComment(body.data.comments), total: body.data.totalCount, page, limit, maxPage: Math.ceil(body.data.totalCount / limit) || 1 };
    },
    async getHotComment({ songmid }, page = 1, limit = 100) {
      if (this._requestObj2) this._requestObj2.cancelHttp();
      const id = "R_SO_4_" + songmid;
      const _requestObj2 = httpFetch(`https://music.163.com/weapi/v1/resource/hotcomments/${id}`, {
        method: "post",
        headers: {
          "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36",
          origin: "https://music.163.com",
          Refere: "http://music.163.com/"
        },
        form: weapi({
          rid: id,
          limit,
          offset: limit * (page - 1),
          beforeTime: Date.now().toString()
        })
      });
      const { body, statusCode } = await _requestObj2.promise;
      if (statusCode != 200 || body.code !== 200) throw new Error("\u83B7\u53D6\u70ED\u95E8\u8BC4\u8BBA\u5931\u8D25");
      const total = body.total ?? 0;
      return { source: "wy", comments: this.filterComment(body.hotComments), total, page, limit, maxPage: Math.ceil(total / limit) || 1 };
    },
    filterComment(rawList) {
      return rawList.map((item) => {
        let data = {
          id: item.commentId,
          text: item.content ? applyEmoji(item.content) : "",
          time: item.time ? item.time : "",
          timeStr: item.time ? dateFormat2(item.time) : "",
          location: item.ipLocation?.location,
          userName: item.user.nickname,
          avatar: item.user.avatarUrl,
          userId: item.user.userId,
          likedCount: item.likedCount,
          reply: []
        };
        let replyData = item.beReplied && item.beReplied[0];
        return replyData ? {
          id: item.commentId,
          rootId: replyData.beRepliedCommentId,
          text: replyData.content ? applyEmoji(replyData.content) : "",
          time: item.time,
          timeStr: null,
          location: replyData.ipLocation?.location,
          userName: replyData.user.nickname,
          avatar: replyData.user.avatarUrl,
          userId: replyData.user.userId,
          likedCount: null,
          reply: [data]
        } : data;
      });
    }
  };

  // src/utils/musicSdk/wy/index.js
  var wy = {
    // tipSearch,
    leaderboard: leaderboard_default4,
    musicSearch: musicSearch_default4,
    songList: songList_default4,
    hotSearch: hotSearch_default4,
    comment: comment_default4,
    getMusicUrl(songInfo, type) {
      return apis("wy").getMusicUrl(songInfo, type);
    },
    getLyric(songInfo) {
      return lyric_default4(songInfo.songmid);
    },
    getPic(songInfo) {
      const requestObj = musicInfo_default2(songInfo.songmid);
      return requestObj.promise.then((info) => info.al.picUrl);
    },
    getMusicDetailPageUrl(songInfo) {
      return `https://music.163.com/#/song?id=${songInfo.songmid}`;
    }
  };
  var wy_default = wy;

  // src/utils/musicSdk/mg/utils/index.js
  var createHttpFetch = async (url, options, retryNum = 0) => {
    if (retryNum > 2) throw new Error("try max num");
    let result;
    try {
      result = await httpFetch(url, options).promise;
    } catch (err) {
      console.log(err);
      return createHttpFetch(url, options, ++retryNum);
    }
    if (result.statusCode !== 200 || (result.body.code !== void 0 ? result.body.code : result.body.returnCode !== void 0 ? result.body.returnCode : result.body.code) !== "000000") return createHttpFetch(url, options, ++retryNum);
    if (result.body.data) return result.body.data;
    return result.body;
  };

  // src/utils/musicSdk/mg/musicInfo.js
  var createGetMusicInfosTask = (ids) => {
    let list = ids;
    let tasks = [];
    while (list.length) {
      tasks.push(list.slice(0, 100));
      if (list.length < 100) break;
      list = list.slice(100);
    }
    let url = "https://c.musicapp.migu.cn/MIGUM2.0/v1.0/content/resourceinfo.do?resourceType=2";
    return Promise.all(tasks.map((task) => createHttpFetch(url, {
      method: "POST",
      form: {
        resourceId: task.join("|")
      }
    }).then((data) => data.resource)));
  };
  var filterMusicInfoList = (rawList) => {
    let ids = /* @__PURE__ */ new Set();
    const list = [];
    rawList.forEach((item) => {
      if (!item.songId || ids.has(item.songId)) return;
      ids.add(item.songId);
      const types = [];
      const _types = {};
      item.newRateFormats?.forEach((type) => {
        let size;
        switch (type.formatType) {
          case "PQ":
            size = sizeFormate(type.size ?? type.androidSize);
            types.push({ type: "128k", size });
            _types["128k"] = {
              size
            };
            break;
          case "HQ":
            size = sizeFormate(type.size ?? type.androidSize);
            types.push({ type: "320k", size });
            _types["320k"] = {
              size
            };
            break;
          case "SQ":
            size = sizeFormate(type.size ?? type.androidSize);
            types.push({ type: "flac", size });
            _types.flac = {
              size
            };
            break;
          case "ZQ":
            size = sizeFormate(type.size ?? type.androidSize);
            types.push({ type: "flac24bit", size });
            _types.flac24bit = {
              size
            };
            break;
        }
      });
      const intervalTest = /(\d\d:\d\d)$/.test(item.length);
      list.push({
        singer: formatSingerName(item.artists, "name"),
        name: item.songName,
        albumName: item.album,
        albumId: item.albumId,
        songmid: item.songId,
        copyrightId: item.copyrightId,
        source: "mg",
        interval: intervalTest ? RegExp.$1 : null,
        img: item.albumImgs?.length ? item.albumImgs[0].img : null,
        lrc: null,
        lrcUrl: item.lrcUrl,
        mrcUrl: item.mrcUrl,
        trcUrl: item.trcUrl,
        otherSource: null,
        types,
        _types,
        typeUrl: {}
      });
    });
    return list;
  };
  var filterMusicInfoListV5 = (rawList) => {
    let ids = /* @__PURE__ */ new Set();
    const list = [];
    rawList.forEach((item) => {
      if (!item.songId || ids.has(item.songId)) return;
      ids.add(item.songId);
      const types = [];
      const _types = {};
      item.audioFormats?.forEach((type) => {
        let size;
        switch (type.formatType) {
          case "PQ":
            size = sizeFormate(type.size ?? type.androidSize);
            types.push({ type: "128k", size });
            _types["128k"] = {
              size
            };
            break;
          case "HQ":
            size = sizeFormate(type.size ?? type.androidSize);
            types.push({ type: "320k", size });
            _types["320k"] = {
              size
            };
            break;
          case "SQ":
            size = sizeFormate(type.size ?? type.androidSize);
            types.push({ type: "flac", size });
            _types.flac = {
              size
            };
            break;
          case "ZQ":
            size = sizeFormate(type.size ?? type.androidSize);
            types.push({ type: "flac24bit", size });
            _types.flac24bit = {
              size
            };
            break;
        }
      });
      list.push({
        singer: formatSingerName(item.singerList, "name"),
        name: item.songName,
        albumName: item.album,
        albumId: item.albumId,
        songmid: item.songId,
        copyrightId: item.copyrightId,
        source: "mg",
        interval: formatPlayTime(item.duration),
        img: item.img3 || item.img2 || item.img1 || null,
        lrc: null,
        lrcUrl: item.lrcUrl,
        mrcUrl: item.mrcUrl,
        trcUrl: item.trcUrl,
        otherSource: null,
        types,
        _types,
        typeUrl: {}
      });
    });
    return list;
  };
  var getMusicInfo = async (copyrightId) => {
    return getMusicInfos([copyrightId]).then((data) => data[0]);
  };
  var getMusicInfos = async (copyrightIds) => {
    return filterMusicInfoList(await Promise.all(createGetMusicInfosTask(copyrightIds)).then((data) => data.flat()));
  };

  // src/utils/musicSdk/mg/leaderboard.js
  var boardList4 = [
    {
      id: "mg__27553319",
      name: "\u65B0\u6B4C\u699C",
      bangid: "27553319",
      source: "mg"
    },
    {
      id: "mg__27186466",
      name: "\u70ED\u6B4C\u699C",
      bangid: "27186466",
      source: "mg"
    },
    {
      id: "mg__27553408",
      name: "\u539F\u521B\u699C",
      bangid: "27553408",
      source: "mg"
    },
    {
      id: "mg__75959118",
      name: "\u97F3\u4E50\u98CE\u5411\u699C",
      bangid: "75959118",
      source: "mg"
    },
    {
      id: "mg__76557036",
      name: "\u5F69\u94C3\u5206\u8D1D\u699C",
      bangid: "76557036",
      source: "mg"
    },
    {
      id: "mg__76557745",
      name: "\u4F1A\u5458\u81FB\u7231\u699C",
      bangid: "76557745",
      source: "mg"
    },
    {
      id: "mg__23189800",
      name: "\u6E2F\u53F0\u699C",
      bangid: "23189800",
      source: "mg"
    },
    {
      id: "mg__23189399",
      name: "\u5185\u5730\u699C",
      bangid: "23189399",
      source: "mg"
    },
    {
      id: "mg__19190036",
      name: "\u6B27\u7F8E\u699C",
      bangid: "19190036",
      source: "mg"
    },
    {
      id: "mg__83176390",
      name: "\u56FD\u98CE\u91D1\u66F2\u699C",
      bangid: "83176390",
      source: "mg"
    }
  ];
  var leaderboard_default5 = {
    limit: 200,
    list: [
      {
        id: "mgyyb",
        name: "\u97F3\u4E50\u699C",
        bangid: "27553319"
      },
      {
        id: "mgysb",
        name: "\u5F71\u89C6\u699C",
        bangid: "23603721"
      },
      {
        id: "mghybnd",
        name: "\u534E\u8BED\u5185\u5730\u699C",
        bangid: "23603926"
      },
      {
        id: "mghyjqbgt",
        name: "\u534E\u8BED\u6E2F\u53F0\u699C",
        bangid: "23603954"
      },
      {
        id: "mgomb",
        name: "\u6B27\u7F8E\u699C",
        bangid: "23603974"
      },
      {
        id: "mgrhb",
        name: "\u65E5\u97E9\u699C",
        bangid: "23603982"
      },
      {
        id: "mgwlb",
        name: "\u7F51\u7EDC\u699C",
        bangid: "23604058"
      },
      {
        id: "mgclb",
        name: "\u5F69\u94C3\u699C",
        bangid: "23604023"
      },
      {
        id: "mgktvb",
        name: "KTV\u699C",
        bangid: "23604040"
      },
      {
        id: "mgrcb",
        name: "\u539F\u521B\u699C",
        bangid: "23604032"
      }
    ],
    getUrl(id, page) {
      return `https://app.c.nf.migu.cn/MIGUM2.0/v1.0/content/querycontentbyId.do?columnId=${id}&needAll=0`;
    },
    successCode: "000000",
    requestBoardsObj: null,
    getBoardsData() {
      if (this.requestBoardsObj) this._requestBoardsObj.cancelHttp();
      this.requestBoardsObj = httpFetch("https://app.c.nf.migu.cn/pc/bmw/rank/rank-index/v1.0", {
        // this.requestBoardsObj = httpFetch('https://app.c.nf.migu.cn/MIGUM3.0/v1.0/template/rank-list/release', {
        // this.requestBoardsObj = httpFetch('https://app.c.nf.migu.cn/MIGUM2.0/v2.0/content/indexrank.do?templateVersion=8', {
        headers: {
          Referer: "https://app.c.nf.migu.cn/",
          "User-Agent": "Mozilla/5.0 (Linux; Android 5.1.1; Nexus 6 Build/LYZ28E) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Mobile Safari/537.36",
          channel: "0146921"
        }
      });
      return this.requestBoardsObj.promise;
    },
    getData(url) {
      const requestObj = httpFetch(url);
      return requestObj.promise;
    },
    // filterBoardsData(listData, list = [], ids = new Set()) {
    //   for (const item of listData) {
    //     if (item.rankId && !ids.has(item.rankId)) {
    //       ids.add(item.rankId)
    //       list.push({
    //         id: 'mg__' + item.rankId,
    //         name: item.rankName,
    //         bangid: String(item.rankId),
    //         source: 'mg',
    //       })
    //     } else if (item.contents) this.filterBoardsData(item.contents, list, ids)
    //   }
    //   return list
    // },
    // filterBoardsData(rawList) {
    //   // console.log(rawList)
    //   let list = []
    //   for (const board of rawList) {
    //     if (board.template != 'group1') continue
    //     for (const item of board.itemList) {
    //       if ((item.template != 'row1' && item.template != 'grid1' && !item.actionUrl) || !item.actionUrl.includes('rank-info')) continue
    //       let data = item.displayLogId.param
    //       list.push({
    //         id: 'mg__' + data.rankId,
    //         name: data.rankName,
    //         bangid: String(data.rankId),
    //       })
    //     }
    //   }
    //   return list
    // },
    async getBoards(retryNum = 0) {
      this.list = boardList4;
      return {
        list: boardList4,
        source: "mg"
      };
    },
    getList(bangid, page, retryNum = 0) {
      if (++retryNum > 3) return Promise.reject(new Error("try max num"));
      return this.getData(this.getUrl(bangid, page)).then(({ statusCode, body }) => {
        if (statusCode !== 200 || body.code !== this.successCode) return this.getList(bangid, page, retryNum);
        const list = filterMusicInfoList(body.columnInfo.contents.map((m) => m.objectInfo));
        return {
          total: list.length,
          list,
          limit: this.limit,
          page,
          source: "mg"
        };
      });
    },
    getDetailPageUrl(id) {
      if (typeof id == "string") id = id.replace("mg__", "");
      for (const item of boardList4) {
        if (item.bangid == id) {
          return `https://music.migu.cn/v3/music/top/${item.webId}`;
        }
      }
      return null;
    }
  };

  // src/utils/musicSdk/mg/musicSearch.js
  var createSignature = (time, str) => {
    const deviceId = "963B7AA0D21511ED807EE5846EC87D20";
    const signatureMd5 = "6cdc72a439cef99a3418d2a78aa28c73";
    const sign = toMD5(`${str}${signatureMd5}yyapp2d16148780a1dcc7408e06336b98cfd50${deviceId}${time}`);
    return { sign, deviceId };
  };
  var musicSearch_default5 = {
    limit: 20,
    total: 0,
    page: 0,
    allPage: 1,
    // 旧版API
    // musicSearch(str, page, limit) {
    //   const searchRequest = httpFetch(`http://pd.musicapp.migu.cn/MIGUM2.0/v1.0/content/search_all.do?ua=Android_migu&version=5.0.1&text=${encodeURIComponent(str)}&pageNo=${page}&pageSize=${limit}&searchSwitch=%7B%22song%22%3A1%2C%22album%22%3A0%2C%22singer%22%3A0%2C%22tagSong%22%3A0%2C%22mvSong%22%3A0%2C%22songlist%22%3A0%2C%22bestShow%22%3A1%7D`, {
    // searchRequest = httpFetch(`http://pd.musicapp.migu.cn/MIGUM2.0/v1.0/content/search_all.do?ua=Android_migu&version=5.0.1&text=${encodeURIComponent(str)}&pageNo=${page}&pageSize=${limit}&searchSwitch=%7B%22song%22%3A1%2C%22album%22%3A0%2C%22singer%22%3A0%2C%22tagSong%22%3A0%2C%22mvSong%22%3A0%2C%22songlist%22%3A0%2C%22bestShow%22%3A1%7D`, {
    // searchRequest = httpFetch(`http://jadeite.migu.cn:7090/music_search/v2/search/searchAll?sid=4f87090d01c84984a11976b828e2b02c18946be88a6b4c47bcdc92fbd40762db&isCorrect=1&isCopyright=1&searchSwitch=%7B%22song%22%3A1%2C%22album%22%3A0%2C%22singer%22%3A0%2C%22tagSong%22%3A1%2C%22mvSong%22%3A0%2C%22bestShow%22%3A1%2C%22songlist%22%3A0%2C%22lyricSong%22%3A0%7D&pageSize=${limit}&text=${encodeURIComponent(str)}&pageNo=${page}&sort=0`, {
    // searchRequest = httpFetch(`https://app.c.nf.migu.cn/MIGUM2.0/v1.0/content/search_all.do?isCopyright=1&isCorrect=1&pageNo=${page}&pageSize=${limit}&searchSwitch={%22song%22:1,%22album%22:0,%22singer%22:0,%22tagSong%22:0,%22mvSong%22:0,%22songlist%22:0,%22bestShow%22:0}&sort=0&text=${encodeURIComponent(str)}`)
    //   // searchRequest = httpFetch(`http://jadeite.migu.cn:7090/music_search/v2/search/searchAll?sid=4f87090d01c84984a11976b828e2b02c18946be88a6b4c47bcdc92fbd40762db&isCorrect=1&isCopyright=1&searchSwitch=%7B%22song%22%3A1%2C%22album%22%3A0%2C%22singer%22%3A0%2C%22tagSong%22%3A1%2C%22mvSong%22%3A0%2C%22bestShow%22%3A1%2C%22songlist%22%3A0%2C%22lyricSong%22%3A0%7D&pageSize=${limit}&text=${encodeURIComponent(str)}&pageNo=${page}&sort=0`, {
    //     headers: {
    //       // sign: 'c3b7ae985e2206e97f1b2de8f88691e2',
    //       // timestamp: 1578225871982,
    //       // appId: 'yyapp2',
    //       // mode: 'android',
    //       // ua: 'Android_migu',
    //       // version: '6.9.4',
    //       osVersion: 'android 7.0',
    //       'User-Agent': 'okhttp/3.9.1',
    //     },
    //   })
    //   // searchRequest = httpFetch(`https://app.c.nf.migu.cn/MIGUM2.0/v1.0/content/search_all.do?isCopyright=1&isCorrect=1&pageNo=${page}&pageSize=${limit}&searchSwitch={%22song%22:1,%22album%22:0,%22singer%22:0,%22tagSong%22:0,%22mvSong%22:0,%22songlist%22:0,%22bestShow%22:0}&sort=0&text=${encodeURIComponent(str)}`)
    //   return searchRequest.promise.then(({ body }) => body)
    // },
    // handleResult(rawData) {
    //   // console.log(rawData)
    //   let ids = new Set()
    //   const list = []
    //   rawData.forEach(item => {
    //     if (ids.has(item.id)) return
    //     ids.add(item.id)
    //     const types = []
    //     const _types = {}
    //     item.newRateFormats && item.newRateFormats.forEach(type => {
    //       let size
    //       switch (type.formatType) {
    //         case 'PQ':
    //           size = sizeFormate(type.size ?? type.androidSize)
    //           types.push({ type: '128k', size })
    //           _types['128k'] = {
    //             size,
    //           }
    //           break
    //         case 'HQ':
    //           size = sizeFormate(type.size ?? type.androidSize)
    //           types.push({ type: '320k', size })
    //           _types['320k'] = {
    //             size,
    //           }
    //           break
    //         case 'SQ':
    //           size = sizeFormate(type.size ?? type.androidSize)
    //           types.push({ type: 'flac', size })
    //           _types.flac = {
    //             size,
    //           }
    //           break
    //         case 'ZQ':
    //           size = sizeFormate(type.size ?? type.androidSize)
    //           types.push({ type: 'flac24bit', size })
    //           _types.flac24bit = {
    //             size,
    //           }
    //           break
    //       }
    //     })
    //     const albumNInfo = item.albums && item.albums.length
    //       ? {
    //           id: item.albums[0].id,
    //           name: item.albums[0].name,
    //         }
    //       : {}
    //     list.push({
    //       singer: this.getSinger(item.singers),
    //       name: item.name,
    //       albumName: albumNInfo.name,
    //       albumId: albumNInfo.id,
    //       songmid: item.songId,
    //       copyrightId: item.copyrightId,
    //       source: 'mg',
    //       interval: null,
    //       img: item.imgItems && item.imgItems.length ? item.imgItems[0].img : null,
    //       lrc: null,
    //       lrcUrl: item.lyricUrl,
    //       mrcUrl: item.mrcurl,
    //       trcUrl: item.trcUrl,
    //       otherSource: null,
    //       types,
    //       _types,
    //       typeUrl: {},
    //     })
    //   })
    //   return list
    // },
    musicSearch(str, page, limit) {
      const time = Date.now().toString();
      const signData = createSignature(time, str);
      const searchRequest = httpFetch(`https://jadeite.migu.cn/music_search/v3/search/searchAll?isCorrect=0&isCopyright=1&searchSwitch=%7B%22song%22%3A1%2C%22album%22%3A0%2C%22singer%22%3A0%2C%22tagSong%22%3A1%2C%22mvSong%22%3A0%2C%22bestShow%22%3A1%2C%22songlist%22%3A0%2C%22lyricSong%22%3A0%7D&pageSize=${limit}&text=${encodeURIComponent(str)}&pageNo=${page}&sort=0&sid=USS`, {
        headers: {
          uiVersion: "A_music_3.6.1",
          deviceId: signData.deviceId,
          timestamp: time,
          sign: signData.sign,
          channel: "0146921",
          "User-Agent": "Mozilla/5.0 (Linux; U; Android 11.0.0; zh-cn; MI 11 Build/OPR1.170623.032) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"
        }
      });
      return searchRequest.promise.then(({ body }) => body);
    },
    filterData(rawData) {
      const list = [];
      const ids = /* @__PURE__ */ new Set();
      rawData.forEach((item) => {
        item.forEach((data) => {
          if (!data.songId || !data.copyrightId || ids.has(data.copyrightId)) return;
          ids.add(data.copyrightId);
          const types = [];
          const _types = {};
          data.audioFormats && data.audioFormats.forEach((type) => {
            let size;
            switch (type.formatType) {
              case "PQ":
                size = sizeFormate(type.asize ?? type.isize);
                types.push({ type: "128k", size });
                _types["128k"] = {
                  size
                };
                break;
              case "HQ":
                size = sizeFormate(type.asize ?? type.isize);
                types.push({ type: "320k", size });
                _types["320k"] = {
                  size
                };
                break;
              case "SQ":
                size = sizeFormate(type.asize ?? type.isize);
                types.push({ type: "flac", size });
                _types.flac = {
                  size
                };
                break;
              case "ZQ24":
                size = sizeFormate(type.asize ?? type.isize);
                types.push({ type: "flac24bit", size });
                _types.flac24bit = {
                  size
                };
                break;
            }
          });
          let img = data.img3 || data.img2 || data.img1 || null;
          if (img && !/https?:/.test(data.img3)) img = "http://d.musicapp.migu.cn" + img;
          list.push({
            singer: formatSingerName(data.singerList),
            name: data.name,
            albumName: data.album,
            albumId: data.albumId,
            songmid: data.songId,
            copyrightId: data.copyrightId,
            source: "mg",
            interval: formatPlayTime(data.duration),
            img,
            lrc: null,
            lrcUrl: data.lrcUrl,
            mrcUrl: data.mrcurl,
            trcUrl: data.trcUrl,
            types,
            _types,
            typeUrl: {}
          });
        });
      });
      return list;
    },
    search(str, page = 1, limit, retryNum = 0) {
      if (++retryNum > 3) return Promise.reject(new Error("try max num"));
      if (limit == null) limit = this.limit;
      return this.musicSearch(str, page, limit).then((result) => {
        if (!result || result.code !== "000000") return Promise.reject(new Error(result ? result.info : "\u641C\u7D22\u5931\u8D25"));
        const songResultData = result.songResultData || { resultList: [], totalCount: 0 };
        let list = this.filterData(songResultData.resultList);
        if (list == null) return this.search(str, page, limit, retryNum);
        this.total = parseInt(songResultData.totalCount);
        this.page = page;
        this.allPage = Math.ceil(this.total / limit);
        return {
          list,
          allPage: this.allPage,
          limit,
          total: this.total,
          source: "mg"
        };
      });
    }
  };

  // src/utils/musicSdk/mg/songList.js
  var songList_default5 = {
    _requestObj_tags: null,
    _requestObj_list: null,
    limit_list: 30,
    limit_song: 30,
    successCode: "000000",
    cachedDetailInfo: {},
    cachedUrl: {},
    sortList: [
      {
        name: "\u63A8\u8350",
        id: "15127315",
        tid: "recommend"
        // id: '1',
      }
      // {
      //   name: '最新',
      //   id: '15127272',
      //   tid: 'new',
      //   // id: '2',
      // },
    ],
    regExps: {
      list: /<li><div class="thumb">.+?<\/li>/g,
      listInfo: /.+data-original="(.+?)".*data-id="(\d+)".*<div class="song-list-name"><a\s.*?>(.+?)<\/a>.+<i class="iconfont cf-bofangliang"><\/i>(.+?)<\/div>/,
      // https://music.migu.cn/v3/music/playlist/161044573?page=1
      listDetailLink: /^.+\/playlist\/(\d+)(?:\?.*|&.*$|#.*$|$)/
    },
    tagsUrl: "https://app.c.nf.migu.cn/pc/v1.0/template/musiclistplaza-taglist/release",
    // tagsUrl: 'https://app.c.nf.migu.cn/MIGUM3.0/v1.0/template/musiclistplaza-taglist/release',
    // tagsUrl: 'https://app.c.nf.migu.cn/MIGUM2.0/v1.0/content/indexTagPage.do?needAll=0',
    getSongListUrl(sortId, tagId, page) {
      if (!tagId) {
        return `https://app.c.nf.migu.cn/pc/bmw/page-data/playlist-square-recommend/v1.0?templateVersion=2&pageNo=${page}`;
      }
      return `https://app.c.nf.migu.cn/pc/v1.0/template/musiclistplaza-listbytag/release?pageNumber=${page}&templateVersion=2&tagId=${tagId}`;
    },
    getSongListDetailUrl(id, page) {
      return `https://app.c.nf.migu.cn/MIGUM3.0/resource/playlist/song/v2.0?pageNo=${page}&pageSize=${this.limit_song}&playlistId=${id}`;
    },
    defaultHeaders: {
      "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1",
      Referer: "https://m.music.migu.cn/"
      // language: 'Chinese',
      // ua: 'Android_migu',
      // mode: 'android',
      // version: '6.8.5',
    },
    getListDetailList(id, page, tryNum = 0) {
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      const requestObj_listDetail = httpFetch(this.getSongListDetailUrl(id, page), { headers: this.defaultHeaders });
      return requestObj_listDetail.promise.then(({ body }) => {
        if (body.code !== this.successCode) return this.getListDetailList(id, page, ++tryNum);
        return {
          list: filterMusicInfoListV5(body.data.songList),
          page,
          limit: this.limit_song,
          total: body.data.totalCount,
          source: "mg"
        };
      });
    },
    getListDetailInfo(id, tryNum = 0) {
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      if (this.cachedDetailInfo[id]) return Promise.resolve(this.cachedDetailInfo[id]);
      const requestObj_listDetailInfo = httpFetch(`https://c.musicapp.migu.cn/MIGUM3.0/resource/playlist/v2.0?playlistId=${id}`, {
        headers: this.defaultHeaders
      });
      return requestObj_listDetailInfo.promise.then(({ body }) => {
        if (body.code !== this.successCode) return this.getListDetail(id, ++tryNum);
        const cachedDetailInfo = this.cachedDetailInfo[id] = {
          name: body.data.title,
          img: body.data.imgItem.img,
          desc: body.data.summary,
          author: body.data.ownerName,
          play_count: formatPlayCount(body.data.opNumItem.playNum)
        };
        return cachedDetailInfo;
      });
    },
    async getDetailUrl(link, page, retryNum = 0) {
      if (retryNum > 3) return Promise.reject(new Error("link try max num"));
      const requestObj_listDetailLink = httpFetch(link, {
        headers: {
          "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1",
          Referer: link
        }
      });
      const { url: location, statusCode } = await requestObj_listDetailLink.promise;
      if (statusCode > 400) return this.getDetailUrl(link, page, ++retryNum);
      if (location.split("?")[0] != link.split("?")[0]) {
        this.cachedUrl[link] = location;
        return this.getListDetail(location, page);
      }
      return Promise.reject(new Error("link get failed"));
    },
    getListDetail(id, page, retryNum = 0) {
      if (/\/playlist[/?]/.test(id)) {
        id = /(?:playlistId|id)=(\d+)/.exec(id)?.[1];
        if (!id) throw new Error("list detail id parse failed");
      } else if (this.regExps.listDetailLink.test(id)) {
        id = id.replace(this.regExps.listDetailLink, "$1");
      } else if (/[?&:/]/.test(id)) {
        const url = this.cachedUrl[id];
        return url ? this.getListDetail(url, page) : this.getDetailUrl(id, page);
      }
      return Promise.all([
        this.getListDetailList(id, page, retryNum),
        this.getListDetailInfo(id, retryNum)
      ]).then(([listData, info]) => {
        listData.info = info;
        return listData;
      });
    },
    // 获取列表数据
    getList(sortId, tagId, page, tryNum = 0) {
      if (this._requestObj_list) this._requestObj_list.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      this._requestObj_list = httpFetch(this.getSongListUrl(sortId, tagId, page), {
        headers: this.defaultHeaders
        // headers: {
        //   sign: 'c3b7ae985e2206e97f1b2de8f88691e2',
        //   timestamp: 1578225871982,
        //   appId: 'yyapp2',
        //   mode: 'android',
        //   ua: 'Android_migu',
        //   version: '6.9.4',
        //   osVersion: 'android 7.0',
        //   'User-Agent': 'okhttp/3.9.1',
        // },
      });
      return this._requestObj_list.promise.then(({ body }) => {
        if (body.code !== "000000") return this.getList(sortId, tagId, page, ++tryNum);
        const list = body.data.contents ? this.filterList2(body.data.contents) : this.filterList(body.data.contentItemList[1].itemList);
        return {
          list,
          total: 99999,
          page,
          limit: this.limit_list,
          source: "mg"
        };
      });
    },
    filterList2(listData, list = [], ids = /* @__PURE__ */ new Set()) {
      for (const item of listData) {
        if (item.contents) this.filterList2(item.contents, list, ids);
        else if (item.resType == "2021" && !ids.has(item.resId)) {
          ids.add(item.resId);
          list.push({
            id: String(item.resId),
            author: "",
            name: item.txt,
            // time: dateFormat(item.createTime, 'Y-M-D'),
            img: item.img,
            // grade: item.grade,
            // total: item.contentCount,
            desc: item.txt2,
            source: "mg"
          });
        }
      }
      return list;
    },
    filterList(rawData) {
      return rawData.map((item) => ({
        play_count: item.barList[0]?.title,
        id: String(item.logEvent.contentId),
        author: "",
        name: item.title,
        // time: dateFormat(item.createTime, 'Y-M-D'),
        img: item.imageUrl,
        // grade: item.grade,
        // total: item.contentCount,
        desc: "",
        source: "mg"
      }));
    },
    // 获取标签
    getTag(tryNum = 0) {
      if (this._requestObj_tags) this._requestObj_tags.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      this._requestObj_tags = httpFetch(this.tagsUrl, { headers: this.defaultHeaders });
      return this._requestObj_tags.promise.then(({ body }) => {
        if (body.code !== this.successCode) return this.getTag(++tryNum);
        return this.filterTagInfo(body.data);
      });
    },
    filterTagInfo(rawList) {
      return {
        hotTag: rawList[0].content.map(({ texts: [name, id] }) => ({
          id,
          name,
          source: "mg"
        })),
        tags: rawList.slice(1).map(({ header, content }) => ({
          name: header.title,
          list: content.map(({ texts: [name, id] }) => ({
            // parent_id: objectInfo.columnId,
            // parent_name: objectInfo.columnTitle,
            id,
            name,
            source: "mg"
          }))
        })),
        source: "mg"
      };
    },
    getTags() {
      return this.getTag();
    },
    getDetailPageUrl(id) {
      if (/playlist\/index\.html\?/.test(id)) {
        id = id.replace(/.*(?:\?|&)id=(\d+)(?:&.*|$)/, "$1");
      } else if (this.regExps.listDetailLink.test(id)) {
        id = id.replace(this.regExps.listDetailLink, "$1");
      }
      return `https://music.migu.cn/v3/music/playlist/${id}`;
    },
    filterSongListResult(raw) {
      const list = [];
      raw.forEach((item) => {
        if (!item.id) return;
        const playCount = parseInt(item.playNum);
        list.push({
          play_count: isNaN(playCount) ? 0 : formatPlayCount(playCount),
          id: item.id,
          author: item.userName,
          name: item.name,
          img: item.musicListPicUrl,
          total: item.musicNum,
          source: "mg"
        });
      });
      return list;
    },
    search(text, page, limit = 20) {
      const timeStr = Date.now().toString();
      const signResult = createSignature(timeStr, text);
      return createHttpFetch(`https://jadeite.migu.cn/music_search/v3/search/searchAll?isCorrect=1&isCopyright=1&searchSwitch=%7B%22song%22%3A0%2C%22album%22%3A0%2C%22singer%22%3A0%2C%22tagSong%22%3A0%2C%22mvSong%22%3A0%2C%22bestShow%22%3A0%2C%22songlist%22%3A1%2C%22lyricSong%22%3A0%7D&pageSize=${limit}&text=${encodeURIComponent(text)}&pageNo=${page}&sort=0&sid=USS`, {
        headers: {
          uiVersion: "A_music_3.6.1",
          deviceId: signResult.deviceId,
          timestamp: timeStr,
          sign: signResult.sign,
          channel: "0146921",
          "User-Agent": "Mozilla/5.0 (Linux; U; Android 11.0.0; zh-cn; MI 11 Build/OPR1.170623.032) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"
        }
      }).then((body) => {
        if (!body.songListResultData) throw new Error("get song list faild.");
        const list = this.filterSongListResult(body.songListResultData.result);
        return {
          list,
          limit,
          total: parseInt(body.songListResultData.totalCount),
          source: "mg"
        };
      });
    }
  };

  // src/utils/musicSdk/mg/pic.js
  var pic_default3 = {
    async getPic(songInfo) {
      const info = await getMusicInfo(songInfo.songmid);
      return info.img;
    }
  };

  // src/utils/musicSdk/mg/utils/mrc.js
  var DELTA = 2654435769n;
  var MIN_LENGTH = 32;
  var keyArr = [
    27303562373562475n,
    18014862372307051n,
    22799692160172081n,
    34058940340699235n,
    30962724186095721n,
    27303523720101991n,
    27303523720101998n,
    31244139033526382n,
    28992395054481524n
  ];
  var teaDecrypt = (data, key) => {
    const length = data.length;
    const lengthBitint = BigInt(length);
    if (length >= 1) {
      let j2 = data[0];
      let j3 = toLong((6n + 52n / lengthBitint) * DELTA);
      while (true) {
        let j4 = j3;
        if (j4 == 0n) break;
        let j5 = toLong(3n & toLong(j4 >> 2n));
        let j6 = lengthBitint;
        while (true) {
          j6--;
          if (j6 > 0n) {
            let j7 = data[j6 - 1n];
            let i = j6;
            j2 = toLong(data[i] - (toLong(toLong(j2 ^ j4) + toLong(j7 ^ key[toLong(toLong(3n & j6) ^ j5)])) ^ toLong(toLong(toLong(j7 >> 5n) ^ toLong(j2 << 2n)) + toLong(toLong(j2 >> 3n) ^ toLong(j7 << 4n)))));
            data[i] = j2;
          } else break;
        }
        let j8 = data[lengthBitint - 1n];
        j2 = toLong(data[0n] - toLong(toLong(toLong(key[toLong(toLong(j6 & 3n) ^ j5)] ^ j8) + toLong(j2 ^ j4)) ^ toLong(toLong(toLong(j8 >> 5n) ^ toLong(j2 << 2n)) + toLong(toLong(j2 >> 3n) ^ toLong(j8 << 4n)))));
        data[0] = j2;
        j3 = toLong(j4 - DELTA);
      }
    }
    return data;
  };
  var longArrToString = (data) => {
    const arrayList = [];
    for (const j of data) arrayList.push(longToBytes(j).toString("utf16le"));
    return arrayList.join("");
  };
  var longToBytes = (l) => {
    const result = Buffer.alloc(8);
    for (let i = 0; i < 8; i++) {
      result[i] = parseInt(l & 0xFFn);
      l >>= 8n;
    }
    return result;
  };
  var toBigintArray = (data) => {
    const length = Math.floor(data.length / 16);
    const jArr = Array(length);
    for (let i = 0; i < length; i++) {
      jArr[i] = toLong(data.substring(i * 16, i * 16 + 16));
    }
    return jArr;
  };
  var MAX = 9223372036854775807n;
  var MIN = -9223372036854775808n;
  var toLong = (str) => {
    const num = typeof str == "string" ? BigInt("0x" + str) : str;
    if (num > MAX) return toLong(num - (1n << 64n));
    else if (num < MIN) return toLong(num + (1n << 64n));
    return num;
  };
  var decrypt = (data) => {
    return data == null || data.length < MIN_LENGTH ? data : longArrToString(teaDecrypt(toBigintArray(data), keyArr));
  };

  // src/utils/musicSdk/mg/lyric.js
  var mrcTools = {
    rxps: {
      lineTime: /^\s*\[(\d+),\d+\]/,
      wordTime: /\(\d+,\d+\)/,
      wordTimeAll: /(\(\d+,\d+\))/g
    },
    parseLyric(str) {
      str = str.replace(/\r/g, "");
      const lines = str.split("\n");
      const lxlrcLines = [];
      const lrcLines = [];
      for (const line of lines) {
        if (line.length < 6) continue;
        let result = this.rxps.lineTime.exec(line);
        if (!result) continue;
        const startTime = parseInt(result[1]);
        let time = startTime;
        let ms = (time % 1e3).toString().padStart(3, "0");
        time /= 1e3;
        let m = parseInt(time / 60).toString().padStart(2, "0");
        time %= 60;
        let s = parseInt(time).toString().padStart(2, "0");
        time = `${m}:${s}.${ms}`;
        let words = line.replace(this.rxps.lineTime, "");
        lrcLines.push(`[${time}]${words.replace(this.rxps.wordTimeAll, "")}`);
        let times = words.match(this.rxps.wordTimeAll);
        if (!times) continue;
        times = times.map((time2) => {
          const result2 = /\((\d+),(\d+)\)/.exec(time2);
          return `<${Math.trunc(parseInt(result2[1]) - startTime)},${result2[2]}>`;
        });
        const wordArr = words.split(this.rxps.wordTime);
        const newWords = times.map((time2, index) => `${time2}${wordArr[index]}`).join("");
        lxlrcLines.push(`[${time}]${newWords}`);
      }
      return {
        lyric: lrcLines.join("\n"),
        lxlyric: lxlrcLines.join("\n")
      };
    },
    getText(url, tryNum = 0) {
      const requestObj = httpFetch(url, {
        headers: {
          Referer: "https://app.c.nf.migu.cn/",
          "User-Agent": "Mozilla/5.0 (Linux; Android 5.1.1; Nexus 6 Build/LYZ28E) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Mobile Safari/537.36",
          channel: "0146921"
        }
      });
      return requestObj.promise.then(({ statusCode, body }) => {
        if (statusCode == 200) return body;
        if (tryNum > 5 || statusCode == 404) return Promise.reject(new Error("\u6B4C\u8BCD\u83B7\u53D6\u5931\u8D25"));
        return this.getText(url, ++tryNum);
      });
    },
    getMrc(url) {
      return this.getText(url).then((text) => {
        return this.parseLyric(decrypt(text));
      });
    },
    getLrc(url) {
      return this.getText(url).then((text) => ({ lxlyric: "", lyric: text }));
    },
    getTrc(url) {
      if (!url) return Promise.resolve("");
      return this.getText(url);
    },
    async getMusicInfo(songInfo) {
      return songInfo.mrcUrl == null ? getMusicInfo(songInfo.songmid) : songInfo;
    },
    getLyric(songInfo) {
      return {
        promise: this.getMusicInfo(songInfo).then((info) => {
          let p;
          if (info.mrcUrl) p = this.getMrc(info.mrcUrl);
          else if (info.lrcUrl) p = this.getLrc(info.lrcUrl);
          if (p == null) return Promise.reject(new Error("\u83B7\u53D6\u6B4C\u8BCD\u5931\u8D25"));
          return Promise.all([p, this.getTrc(info.trcUrl)]).then(([lrcInfo, tlyric]) => {
            lrcInfo.tlyric = tlyric;
            return lrcInfo;
          });
        }),
        cancelHttp() {
        }
      };
    }
  };
  var lyric_default5 = {
    getLyric(songInfo) {
      let requestObj = mrcTools.getLyric(songInfo);
      return requestObj;
    }
  };

  // src/utils/musicSdk/mg/hotSearch.js
  var hotSearch_default5 = {
    _requestObj: null,
    async getList(retryNum = 0) {
      if (this._requestObj) this._requestObj.cancelHttp();
      if (retryNum > 2) return Promise.reject(new Error("try max num"));
      const _requestObj = httpFetch("http://jadeite.migu.cn:7090/music_search/v3/search/hotword");
      const { body, statusCode } = await _requestObj.promise;
      if (statusCode != 200 || body.code !== "000000") throw new Error("\u83B7\u53D6\u70ED\u641C\u8BCD\u5931\u8D25");
      return { source: "mg", list: this.filterList(body.data.hotwords[0].hotwordList) };
    },
    filterList(rawList) {
      return rawList.filter((item) => item.resourceType == "song").map((item) => item.word);
    }
  };

  // src/utils/musicSdk/mg/songId.js
  var getSongId = async (mInfo) => {
    if (mInfo.songmid != mInfo.copyrightId) return mInfo.songmid;
    const musicInfo = await getMusicInfo(mInfo.songmid);
    return musicInfo.songmid;
  };
  var songId_default = getSongId;

  // src/utils/musicSdk/mg/comment.js
  var comment_default5 = {
    _requestObj: null,
    _requestObj2: null,
    _requestObj3: null,
    lastCommentIds: /* @__PURE__ */ new Map(),
    async getComment(musicInfo, page = 1, limit = 20) {
      if (this._requestObj) this._requestObj.cancelHttp();
      if (!musicInfo.songId) {
        let id = await songId_default(musicInfo);
        if (!id) throw new Error("\u83B7\u53D6\u8BC4\u8BBA\u5931\u8D25");
        musicInfo.songId = id;
      }
      if (page === 1) this.lastCommentIds.clear();
      const lastCommentId = this.lastCommentIds.get(String(page)) || "";
      if (!lastCommentId && page > 1) throw new Error("\u83B7\u53D6\u8BC4\u8BBA\u5931\u8D25");
      const _requestObj = httpFetch(`https://app.c.nf.migu.cn/MIGUM3.0/user/comment/stack/v1.0?pageSize=${limit}&queryType=1&resourceId=${musicInfo.songId}&resourceType=2&commentId=${lastCommentId}`, {
        headers: {
          "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1"
          // Referer: 'https://music.migu.cn',
        }
      });
      const { body, statusCode } = await _requestObj.promise;
      if (statusCode != 200 || body.code !== "000000") throw new Error("\u83B7\u53D6\u8BC4\u8BBA\u5931\u8D25");
      const total = parseInt(body.data.commentNums);
      const list = this.filterComment(body.data.comments);
      this.lastCommentIds.set(String(page + 1), list.length ? list[list.length - 1].id : "");
      return { source: "mg", comments: list, total, page, limit, maxPage: Math.ceil(total / limit) || 1 };
    },
    async getHotComment(musicInfo, page = 1, limit = 20) {
      if (this._requestObj2) this._requestObj2.cancelHttp();
      if (!musicInfo.songId) {
        let id = await songId_default(musicInfo);
        if (!id) throw new Error("\u83B7\u53D6\u8BC4\u8BBA\u5931\u8D25");
        musicInfo.songId = id;
      }
      const _requestObj2 = httpFetch(`https://app.c.nf.migu.cn/MIGUM3.0/user/comment/stack/v1.0?pageSize=${limit}&queryType=2&resourceId=${musicInfo.songId}&resourceType=2&hotCommentStart=${(page - 1) * limit}`, {
        headers: {
          "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1"
          // Referer: 'https://music.migu.cn',
        }
      });
      const { body, statusCode } = await _requestObj2.promise;
      if (statusCode != 200 || body.code !== "000000") throw new Error("\u83B7\u53D6\u70ED\u95E8\u8BC4\u8BBA\u5931\u8D25");
      const total = parseInt(body.data.cfgHotCount);
      return { source: "mg", comments: this.filterComment(body.data.hotComments), total, page, limit, maxPage: Math.ceil(total / limit) || 1 };
    },
    async getReplyComment(musicInfo, replyId, page = 1, limit = 10) {
      if (this._requestObj2) this._requestObj2.cancelHttp();
      const _requestObj2 = httpFetch(`https://app.c.nf.migu.cn/MIGUM3.0/user/comment/stack/${replyId}/v1.0?pageSize=${limit}&queryType=2&resourceId=${musicInfo.songId}&resourceType=2&start=${(page - 1) * limit}`, {
        headers: {
          "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1"
        }
      });
      const { body, statusCode } = await _requestObj2.promise;
      if (statusCode != 200 || body.code !== "000000") throw new Error("\u83B7\u53D6\u56DE\u590D\u8BC4\u8BBA\u5931\u8D25");
      const total = parseInt(body.data.replyTotalCount);
      return { source: "mg", comments: this.filterComment(body.data.mainCommentItem.replyComments), total, page, limit, maxPage: Math.ceil(total / limit) || 1 };
    },
    filterComment(rawList) {
      return rawList.map((item) => ({
        id: item.commentId,
        text: item.commentInfo,
        time: item.commentTime,
        timeStr: dateFormat2(new Date(item.commentTime).getTime()),
        userName: item.user.nickName,
        avatar: item.user.middleIcon || item.user.bigIcon || item.user.smallIcon,
        userId: item.user.userId,
        likedCount: item.opNumItem.thumbNum,
        replyNum: item.replyTotalCount,
        reply: item.replyComments.map((c) => ({
          id: c.replyId,
          text: c.replyInfo,
          time: c.replyTime,
          timeStr: dateFormat2(new Date(c.replyTime).getTime()),
          userName: c.user.nickName,
          avatar: c.user.middleIcon || c.user.bigIcon || c.user.smallIcon,
          userId: c.user.userId,
          likedCount: null,
          replyNum: null
        }))
      }));
    }
  };

  // src/utils/musicSdk/mg/index.js
  var mg = {
    // tipSearch,
    songList: songList_default5,
    musicSearch: musicSearch_default5,
    leaderboard: leaderboard_default5,
    hotSearch: hotSearch_default5,
    comment: comment_default5,
    getMusicUrl(songInfo, type) {
      return apis("mg").getMusicUrl(songInfo, type);
    },
    getLyric(songInfo) {
      return lyric_default5.getLyric(songInfo);
    },
    getPic(songInfo) {
      return pic_default3.getPic(songInfo);
    },
    getMusicDetailPageUrl(songInfo) {
      return `http://music.migu.cn/v3/music/song/${songInfo.copyrightId}`;
    }
  };
  var mg_default = mg;

  // src/utils/musicSdk/xm.js
  var xm = {
    // songList,
    // musicSearch,
    // leaderboard,
    // hotSearch,
    // closeVerifyModal,
    comment: {
      getComment() {
        return Promise.reject(new Error("fail"));
      },
      getHotComment() {
        return Promise.reject(new Error("fail"));
      }
    },
    getMusicUrl(songInfo, type) {
      return {
        promise: Promise.reject(new Error("fail"))
      };
    },
    getLyric(songInfo) {
      return {
        promise: Promise.reject(new Error("fail"))
      };
    },
    getPic(songInfo) {
      return Promise.reject(new Error("fail"));
    }
    // getMusicDetailPageUrl(songInfo) {
    //   if (songInfo.songStringId) return `https://www.xiami.com/song/${songInfo.songStringId}`
    //   musicInfo.getMusicInfo(songInfo).then(({ data }) => {
    //     songInfo.songStringId = data.songStringId
    //   })
    //   return `https://www.xiami.com/song/${songInfo.songmid}`
    // },
    // init() {
    //   getToken()
    // },
  };
  var xm_default = xm;

  // src/utils/musicSdk/index.js
  var sources2 = {
    sources: [
      {
        name: "\u9177\u6211\u97F3\u4E50",
        id: "kw"
      },
      {
        name: "\u9177\u72D7\u97F3\u4E50",
        id: "kg"
      },
      {
        name: "QQ\u97F3\u4E50",
        id: "tx"
      },
      {
        name: "\u7F51\u6613\u97F3\u4E50",
        id: "wy"
      },
      {
        name: "\u54AA\u5495\u97F3\u4E50",
        id: "mg"
      }
      // {
      //   name: '百度音乐',
      //   id: 'bd',
      // },
    ],
    kw: kw_default,
    kg: kg_default,
    tx: tx_default,
    wy: wy_default,
    mg: mg_default,
    // bd,
    xm: xm_default
  };
  var musicSdk_default = {
    ...sources2,
    supportQuality
  };

  // entry.js
  var entry_default = musicSdk_default;
  return __toCommonJS(entry_exports);
})();
/*! Bundled license information:

ieee754/index.js:
  (*! ieee754. BSD-3-Clause License. Feross Aboukhadijeh <https://feross.org/opensource> *)

buffer/index.js:
  (*!
   * The buffer module from node.js, for the browser.
   *
   * @author   Feross Aboukhadijeh <https://feross.org>
   * @license  MIT
   *)
*/
