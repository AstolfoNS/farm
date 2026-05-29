(function (window, $) {
    var VERSION = "1.0.0";
    var moduleRegistry = {};
    var context = {
        bootedAt: null
    };

    function safeString(value) {
        return value == null ? "" : String(value);
    }

    function nowIso() {
        return new Date().toISOString();
    }

    function log(level, message, meta) {
        if (!window.console || !$.isFunction(window.console.log)) {
            return;
        }
        var text = "[FarmCore][" + safeString(level).toUpperCase() + "] " + safeString(message);
        if (meta === undefined) {
            window.console.log(text);
            return;
        }
        window.console.log(text, meta);
    }

    function normalizeModuleName(name) {
        return safeString(name).trim().toLowerCase();
    }

    function registerModule(name, handlers) {
        var moduleName = normalizeModuleName(name);
        if (!moduleName) {
            return false;
        }
        moduleRegistry[moduleName] = $.extend({}, moduleRegistry[moduleName] || {}, handlers || {});
        return true;
    }

    function getModule(name) {
        return moduleRegistry[normalizeModuleName(name)] || null;
    }

    function listModules() {
        var names = [];
        $.each(moduleRegistry, function (name) {
            names.push(name);
        });
        names.sort();
        return names;
    }

    function invokeModule(name, hook, args) {
        var moduleName = normalizeModuleName(name);
        var entry = getModule(moduleName);
        if (!entry || !$.isFunction(entry[hook])) {
            return false;
        }
        try {
            entry[hook].apply(entry, args || []);
            return true;
        } catch (e) {
            log("error", "module hook failed: " + moduleName + "." + hook, e);
            emit("core:error", {
                module: moduleName,
                hook: hook,
                message: e && e.message ? e.message : "unknown error"
            });
            return false;
        }
    }

    function activateModule(name) {
        return invokeModule(name, "activate", [name]);
    }

    function deactivateModule(name) {
        return invokeModule(name, "deactivate", [name]);
    }

    function refreshModule(name) {
        return invokeModule(name, "refresh", [name]);
    }

    function emit(eventName, payload) {
        var name = safeString(eventName).trim();
        if (!name) {
            return;
        }
        $(document).trigger(name, [payload || {}]);
    }

    function on(eventName, handler) {
        var name = safeString(eventName).trim();
        if (!name || !$.isFunction(handler)) {
            return;
        }
        $(document).on(name, function (_, payload) {
            handler(payload || {});
        });
    }

    function setContext(key, value) {
        var name = safeString(key).trim();
        if (!name) {
            return;
        }
        context[name] = value;
    }

    function getContext(key, defValue) {
        var name = safeString(key).trim();
        if (!name) {
            return defValue;
        }
        return context[name] === undefined ? defValue : context[name];
    }

    function snapshot() {
        return {
            version: VERSION,
            bootedAt: context.bootedAt,
            modules: listModules()
        };
    }

    function boot() {
        if (context.bootedAt) {
            return;
        }
        context.bootedAt = nowIso();
        log("info", "frontend core booted", snapshot());
        emit("core:booted", snapshot());
    }

    window.FarmCore = {
        version: VERSION,
        boot: boot,
        log: log,
        registerModule: registerModule,
        getModule: getModule,
        listModules: listModules,
        activateModule: activateModule,
        deactivateModule: deactivateModule,
        refreshModule: refreshModule,
        emit: emit,
        on: on,
        setContext: setContext,
        getContext: getContext,
        snapshot: snapshot
    };
})(window, window.jQuery);
