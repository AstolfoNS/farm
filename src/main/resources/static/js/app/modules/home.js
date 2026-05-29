(function (window, $) {
    var switchingUser = false;
    var userSelectState = {
        rows: [],
        selectedId: 0,
        panelOpen: false
    };
    var supportedModules = {
        home: true,
        "user-manage": true,
        "user-select": true,
        farm: true,
        "plot-admin": true,
        shop: true,
        store: true,
        "seed-admin": true,
        settings: true
    };
    var moduleLifecycleMap = {};

    function motion() {
        if (window.FarmUi && $.isFunction(window.FarmUi.motion)) {
            return window.FarmUi.motion();
        }
        if ($.isFunction(window.farmMotion)) {
            return window.farmMotion();
        }
        return {moduleEnterMs: 260, actionFeedbackMs: 1200};
    }

    function showPanel($el) {
        if (window.FarmUi && $.isFunction(window.FarmUi.showPanel)) {
            window.FarmUi.showPanel($el);
            return;
        }
        $el.stop(true, true).css("display", "none").fadeIn(motion().moduleEnterMs);
    }

    function hidePanel($el) {
        if (window.FarmUi && $.isFunction(window.FarmUi.hidePanel)) {
            window.FarmUi.hidePanel($el);
            return;
        }
        $el.stop(true, true).fadeOut(motion().moduleEnterMs);
    }

    function asNumber(value, def) {
        if (window.FarmUi && $.isFunction(window.FarmUi.asNumber)) {
            return window.FarmUi.asNumber(value, def);
        }
        var n = Number(value);
        return isNaN(n) ? (def || 0) : n;
    }

    function escapeHtml(value) {
        return $("<div/>").text(value == null ? "" : String(value)).html();
    }

    function resolveHead(user) {
        var head = user && user.head ? String(user.head).trim() : "";
        return head.length > 0 ? head : farmResolveImg("app/user/default-avatar.png");
    }

    function renderTopUser(user) {
        var data = user || {};
        $("#topUserAvatar").attr("src", resolveHead(data));
        $("#topUserName").text(data.nickname || "未知用户");
        $("#topUserExp").text(asNumber(data.experience, 0));
        $("#topUserCoin").text(asNumber(data.coin, 0));
        $("#topUserScore").text(asNumber(data.score, 0));
    }

    function currentUserId() {
        var user = window.FarmAppState.currentUser || {};
        return asNumber(user.id, 0);
    }

    function syncRealtime() {
        if (window.FarmWsBridge && $.isFunction(window.FarmWsBridge.syncUser)) {
            window.FarmWsBridge.syncUser(currentUserId());
        }
    }

    function syncUserSelectValue(user) {
        if (!user || !user.id) {
            return;
        }
        userSelectState.selectedId = asNumber(user.id, 0);
        renderUserSelectDisplay();
        markCurrentUserSelectRow();
    }

    function refreshCurUser(onDone) {
        FarmApi.getCurUser(function (res) {
            if (!(FarmApi.isOk(res) && res.data)) {
                if ($.isFunction(onDone)) {
                    onDone();
                }
                return;
            }
            window.FarmAppState.currentUser = res.data;
            renderTopUser(res.data);
            syncUserSelectValue(res.data);
            syncRealtime();
            if (window.FarmAudio && $.isFunction(window.FarmAudio.reloadSettings)) {
                window.FarmAudio.reloadSettings();
            }
            if (window.FarmAppState.currentModule === "farm" && window.FarmModule) {
                window.FarmModule.loadOverviewByUser(currentUserId(), true);
            }
            if (window.FarmAppState.currentModule === "shop" && window.FarmShopModule) {
                window.FarmShopModule.reload();
            }
            if (window.FarmAppState.currentModule === "store" && window.FarmStoreModule) {
                window.FarmStoreModule.reload();
            }
            if (window.FarmCore && $.isFunction(window.FarmCore.emit)) {
                window.FarmCore.emit("user:changed", {
                    user: res.data || {},
                    userId: asNumber(res.data && res.data.id, 0)
                });
            }
            if ($.isFunction(onDone)) {
                onDone(res.data);
            }
        }, function () {
            if ($.isFunction(onDone)) {
                onDone();
            }
        });
    }

    function userRowFormatter(row) {
        var nickname = row && row.nickname ? row.nickname : "";
        var username = row && row.username ? row.username : "";
        var exp = asNumber(row && row.experience, 0);
        var coin = asNumber(row && row.coin, 0);
        var score = asNumber(row && row.score, 0);
        var expIcon = farmResolveImg("app/user/stat-exp.png");
        var coinIcon = farmResolveImg("app/user/stat-gold.png");
        var scoreIcon = farmResolveImg("app/user/stat-score.png");
        return "<div class='user-select-option'>" +
            "<span class='user-select-col user-select-col-avatar'><img class='user-select-option-avatar' src='" + escapeHtml(resolveHead(row)) + "' alt=''></span>" +
            "<span class='user-select-col user-select-col-username'>" + escapeHtml(username) + "</span>" +
            "<span class='user-select-col user-select-col-nickname'>" + escapeHtml(nickname) + "</span>" +
            "<span class='user-select-col user-select-col-stat user-select-col-exp'>" +
            "<img class='user-select-stat-icon' src='" + escapeHtml(expIcon) + "' alt='exp'>" +
            "<span class='user-select-stat-label'>经验:</span><em class='user-select-stat-value'>" + exp + "</em>" +
            "</span>" +
            "<span class='user-select-col user-select-col-stat user-select-col-coin'>" +
            "<img class='user-select-stat-icon' src='" + escapeHtml(coinIcon) + "' alt='coin'>" +
            "<span class='user-select-stat-label'>金币:</span><em class='user-select-stat-value'>" + coin + "</em>" +
            "</span>" +
            "<span class='user-select-col user-select-col-stat user-select-col-score'>" +
            "<img class='user-select-stat-icon' src='" + escapeHtml(scoreIcon) + "' alt='score'>" +
            "<span class='user-select-stat-label'>积分:</span><em class='user-select-stat-value'>" + score + "</em>" +
            "</span>" +
            "</div>";
    }

    function userInputText(row) {
        var nickname = row && row.nickname ? row.nickname : "";
        var username = row && row.username ? row.username : "";
        return "[" + username + "]" + nickname;
    }

    function userRowById(userId) {
        var targetId = asNumber(userId, 0);
        var matched = null;
        $.each(userSelectState.rows, function (_, row) {
            if (asNumber(row.id, 0) === targetId) {
                matched = row;
                return false;
            }
            return true;
        });
        return matched;
    }

    function renderUserSelectDisplay() {
        var row = userRowById(userSelectState.selectedId);
        var text = row ? (row.displayText || userInputText(row)) : "请选择用户";
        $("#homeUserSelectText").text(text);
    }

    function markCurrentUserSelectRow() {
        var userId = asNumber(userSelectState.selectedId, 0);
        var $rows = $("#homeUserSelectPanel .user-select-native-row");
        $rows.removeClass("is-selected");
        if (userId <= 0) {
            return;
        }
        $rows.filter("[data-user-id='" + userId + "']").addClass("is-selected");
    }

    function renderUserSelectPanel() {
        var html = [];
        $.each(userSelectState.rows, function (_, row) {
            html.push(
                "<div class='user-select-native-row' data-user-id='" + asNumber(row.id, 0) + "'>" +
                userRowFormatter(row) +
                "</div>"
            );
        });
        if (html.length <= 0) {
            html.push("<div class='user-select-native-row'><div class='user-select-option'><span class='user-select-col'>暂无用户数据</span></div></div>");
        }
        $("#homeUserSelectPanel").html(html.join(""));
        markCurrentUserSelectRow();
    }

    function toggleUserSelectPanel(openFlag) {
        var shouldOpen = openFlag === true;
        var $panel = $("#homeUserSelectPanel");
        if (!shouldOpen) {
            userSelectState.panelOpen = false;
            $panel.stop(true, true).fadeOut(120);
            return;
        }
        userSelectState.panelOpen = true;
        repaintUserSelect();
        $panel.stop(true, true).fadeIn(120);
    }

    function repaintUserSelect() {
        var width = $("#homePanel .user-select-input-row").innerWidth();
        if (width > 0) {
            $("#homeUserSelectPanel").css("width", width + "px");
        }
        renderUserSelectDisplay();
        markCurrentUserSelectRow();
    }

    function normalizeUserOptions(rawRows) {
        var rows = $.isArray(rawRows) ? rawRows : [];
        var mapped = [];
        $.each(rows, function (idx, row) {
            var item = $.extend({}, row);
            item.displayText = userInputText(row);
            mapped.push(item);
        });
        return mapped;
    }

    function loadUserOptions() {
        FarmApi.loginOptions(function (res) {
            var rows = (FarmApi.isOk(res) && $.isArray(res.data)) ? normalizeUserOptions(res.data) : [];
            userSelectState.rows = rows;
            var curUser = window.FarmAppState.currentUser || {};
            var curId = asNumber(curUser.id, 0);
            if (curId > 0) {
                userSelectState.selectedId = curId;
            } else if (rows.length > 0) {
                userSelectState.selectedId = asNumber(rows[0].id, 0);
            } else {
                userSelectState.selectedId = 0;
            }
            renderUserSelectPanel();
            repaintUserSelect();
        }, function () {
            userSelectState.rows = [];
            userSelectState.selectedId = 0;
            renderUserSelectPanel();
            repaintUserSelect();
        });
    }

    function initUserSelect() {
        $("#homeUserSelect").on("click", function (event) {
            event.stopPropagation();
            toggleUserSelectPanel(!userSelectState.panelOpen);
        });
        $("#homeUserSelectPanel").on("click", ".user-select-native-row", function (event) {
            event.stopPropagation();
            var userId = asNumber($(this).attr("data-user-id"), 0);
            if (userId <= 0) {
                return;
            }
            userSelectState.selectedId = userId;
            renderUserSelectDisplay();
            markCurrentUserSelectRow();
            toggleUserSelectPanel(false);
        });
        $(document).on("click", function () {
            toggleUserSelectPanel(false);
        });
        loadUserOptions();
    }

    function applyShellBackground(moduleName) {
        var isHome = moduleName === "home";
        $("#appShell").toggleClass("is-homepage", isHome);
        $("#appShell").toggleClass("is-subpage", !isHome);
    }

    function setTopNav(moduleName) {
        $(".topbar-nav-item").removeClass("is-active");
        if (moduleName !== "home") {
            $(".topbar-nav-item[data-module='" + moduleName + "']").addClass("is-active");
        }
    }

    function setFarmModuleActive(flag) {
        if (window.FarmModule && $.isFunction(window.FarmModule.setActive)) {
            window.FarmModule.setActive(flag);
        }
    }

    function setShopModuleActive(flag) {
        if (window.FarmShopModule && $.isFunction(window.FarmShopModule.setActive)) {
            window.FarmShopModule.setActive(flag);
        }
    }

    function setStoreModuleActive(flag) {
        if (window.FarmStoreModule && $.isFunction(window.FarmStoreModule.setActive)) {
            window.FarmStoreModule.setActive(flag);
        }
    }

    function setPlotAdminActive(flag) {
        if (window.FarmPlotAdminModule && $.isFunction(window.FarmPlotAdminModule.setActive)) {
            window.FarmPlotAdminModule.setActive(flag);
            return;
        }
        if (flag) {
            showPanel($("#plotAdminPanel"));
            return;
        }
        hidePanel($("#plotAdminPanel"));
    }

    function setUserManageActive(flag) {
        if (window.FarmUserAdminModule && $.isFunction(window.FarmUserAdminModule.setActive)) {
            window.FarmUserAdminModule.setActive(flag);
            return;
        }
        if (flag) {
            showPanel($("#userManagePanel"));
            return;
        }
        hidePanel($("#userManagePanel"));
    }

    function setUserSelectActive(flag) {
        if (flag) {
            loadUserOptions();
            showPanel($("#homePanel"));
            window.setTimeout(function () {
                repaintUserSelect();
            }, motion().moduleEnterMs + 20);
            return;
        }
        toggleUserSelectPanel(false);
        hidePanel($("#homePanel"));
    }

    function setSeedAdminActive(flag) {
        if (window.FarmSeedAdminModule && $.isFunction(window.FarmSeedAdminModule.setActive)) {
            window.FarmSeedAdminModule.setActive(flag);
            return;
        }
        if (flag) {
            showPanel($("#seedAdminPanel"));
            return;
        }
        hidePanel($("#seedAdminPanel"));
    }

    function setSettingsActive(flag) {
        if (flag) {
            showPanel($("#settingsPanel"));
            if (window.FarmAudio && $.isFunction(window.FarmAudio.renderSettings)) {
                window.FarmAudio.renderSettings();
            }
            return;
        }
        hidePanel($("#settingsPanel"));
    }

    function buildModuleLifecycleMap() {
        return {
            "user-select": {
                activate: function () { setUserSelectActive(true); },
                deactivate: function () { setUserSelectActive(false); }
            },
            settings: {
                activate: function () { setSettingsActive(true); },
                deactivate: function () { setSettingsActive(false); }
            }
        };
    }

    function registerCoreModules() {
        moduleLifecycleMap = buildModuleLifecycleMap();
        if (!(window.FarmCore && $.isFunction(window.FarmCore.registerModule))) {
            return;
        }
        $.each(moduleLifecycleMap, function (moduleName, lifecycle) {
            window.FarmCore.registerModule(moduleName, lifecycle);
        });
    }

    function resolveLifecycle(moduleName) {
        if (window.FarmCore && $.isFunction(window.FarmCore.getModule)) {
            var entry = window.FarmCore.getModule(moduleName);
            if (entry) {
                return entry;
            }
        }
        return moduleLifecycleMap[moduleName] || null;
    }

    function activateModule(moduleName) {
        if (moduleName === "home") {
            return;
        }
        var lifecycle = resolveLifecycle(moduleName);
        if (lifecycle && $.isFunction(lifecycle.activate)) {
            lifecycle.activate();
            return;
        }
        $.messager.show({
            title: "提示",
            msg: "模块 " + moduleName + " 正在重构中�?,
            timeout: motion().actionFeedbackMs,
            showType: "slide"
        });
    }

    function deactivateModule(moduleName) {
        if (moduleName === "home") {
            return;
        }
        var lifecycle = resolveLifecycle(moduleName);
        if (lifecycle && $.isFunction(lifecycle.deactivate)) {
            lifecycle.deactivate();
        }
    }

    function switchModule(moduleName, options) {
        var opts = options || {};
        var nextModule = supportedModules[moduleName] ? moduleName : "home";
        var prevModule = window.FarmAppState.currentModule || "home";
        if (nextModule === prevModule && opts.force !== true) {
            return;
        }
        window.FarmAppState.currentModule = nextModule;
        if (window.FarmCore && $.isFunction(window.FarmCore.setContext)) {
            window.FarmCore.setContext("currentModule", nextModule);
            window.FarmCore.setContext("previousModule", prevModule);
        }
        applyShellBackground(nextModule);
        setTopNav(nextModule);
        deactivateModule(prevModule);
        activateModule(nextModule);
        if (window.FarmCore && $.isFunction(window.FarmCore.emit)) {
            window.FarmCore.emit("module:changed", {
                previous: prevModule,
                current: nextModule
            });
        }
    }

    function selectedUserForSwitch() {
        var userId = asNumber(userSelectState.selectedId, 0);
        if (userId <= 0) {
            return null;
        }
        var target = userRowById(userId);
        return target || {id: userId};
    }

    function bindEvents() {
        $(".topbar-nav-item").off("click").on("click", function (event) {
            event.preventDefault();
            if (window.FarmAudio && $.isFunction(window.FarmAudio.play)) {
                window.FarmAudio.play("click");
            }
            switchModule($(this).data("module"));
        });

        $("#homeConfirmUserBtn").off("click").on("click", function (event) {
            event.preventDefault();
            event.stopPropagation();
            if (switchingUser) {
                return;
            }
            var row = selectedUserForSwitch();
            if (!row || !row.id) {
                $.messager.show({
                    title: "消息",
                    msg: "请先选择用户",
                    timeout: motion().actionFeedbackMs,
                    showType: "slide"
                });
                return;
            }
            switchingUser = true;
            $("#homeConfirmUserBtn").linkbutton("disable");
            function finishSwitch() {
                switchingUser = false;
                $("#homeConfirmUserBtn").linkbutton("enable");
            }
            FarmApi.setCurUser(row.id, function (res) {
                if (!FarmApi.isOk(res)) {
                    $.messager.alert("提示", (res && res.msg) ? res.msg : "切换失败");
                    finishSwitch();
                    return;
                }
                refreshCurUser(function (curUser) {
                    var nickname = (curUser && curUser.nickname) ? curUser.nickname : (row.nickname || "");
                    var username = (curUser && curUser.username) ? curUser.username : (row.username || "");
                    $.messager.show({
                        title: "消息",
                        msg: "当前用户已经设定为：" + nickname + "[" + username + "]",
                        timeout: motion().actionFeedbackMs,
                        showType: "slide"
                    });
                    if (window.FarmAudio && $.isFunction(window.FarmAudio.play)) {
                        window.FarmAudio.play("click");
                    }
                    finishSwitch();
                });
            }, function () {
                $.messager.alert("提示", "切换失败，请稍后重试");
                finishSwitch();
            });
        });

        $(window).off("resize.homeUserSelect").on("resize.homeUserSelect", function () {
            if (window.FarmAppState.currentModule === "user-select") {
                repaintUserSelect();
            }
        });
    }

    function resolveInitialModule() {
        var moduleName = "home";
        try {
            var query = new URLSearchParams(window.location.search || "");
            var mod = String(query.get("module") || "").toLowerCase();
            if (mod === "profile") {
                mod = "user-manage";
            }
            if (supportedModules[mod]) {
                moduleName = mod;
            }
        } catch (e) {
        }
        return moduleName;
    }

    $(function () {
        if (window.FarmCore && $.isFunction(window.FarmCore.boot)) {
            window.FarmCore.boot();
        }
        registerCoreModules();
        initUserSelect();
        bindEvents();
        switchModule(resolveInitialModule());
        refreshCurUser();
    });

    window.FarmHomeBridge = {
        refreshCurUser: refreshCurUser,
        currentUserId: currentUserId,
        switchModule: switchModule,
        reloadUserOptions: loadUserOptions
    };
})(window, window.jQuery);
