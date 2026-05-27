(function (window, $) {
    var switchingUser = false;

    function motion() {
        if ($.isFunction(window.farmMotion)) {
            return window.farmMotion();
        }
        return {moduleEnterMs: 260, actionFeedbackMs: 1200};
    }

    function showPanel($el) {
        $el.stop(true, true).css("display", "none").fadeIn(motion().moduleEnterMs);
    }

    function hidePanel($el) {
        $el.stop(true, true).fadeOut(motion().moduleEnterMs);
    }

    function asNumber(value, def) {
        var n = Number(value);
        return isNaN(n) ? (def || 0) : n;
    }

    function escapeHtml(value) {
        return $("<div/>").text(value == null ? "" : String(value)).html();
    }

    function resolveHead(user) {
        var head = user && user.head ? String(user.head).trim() : "";
        return head.length > 0 ? head : farmResolveImg("ui/user/default-avatar.png");
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
        if (!$("#homeUserSelect").data("combobox")) {
            return;
        }
        if (!user || !user.id) {
            return;
        }
        $("#homeUserSelect").combobox("setValue", user.id);
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
        var expIcon = farmResolveImg("ui/user/stat-exp.png");
        var coinIcon = farmResolveImg("ui/user/stat-gold.png");
        var scoreIcon = farmResolveImg("ui/user/stat-score.png");
        return "<div class='user-select-option'>" +
            "<img class='user-select-option-avatar' src='" + escapeHtml(resolveHead(row)) + "' alt=''>" +
            "<span class='user-select-option-text'>" +
            "[" + escapeHtml(username) + "]" + escapeHtml(nickname) +
            "<span class='user-select-stat'><img class='user-select-stat-icon' src='" + escapeHtml(expIcon) + "' alt='exp'>经验: " + exp + "</span>" +
            "<span class='user-select-stat'><img class='user-select-stat-icon' src='" + escapeHtml(coinIcon) + "' alt='coin'>金币: " + coin + "</span>" +
            "<span class='user-select-stat'><img class='user-select-stat-icon' src='" + escapeHtml(scoreIcon) + "' alt='score'>积分: " + score + "</span>" +
            "</span>" +
            "</div>";
    }

    function userInputText(row) {
        var nickname = row && row.nickname ? row.nickname : "";
        var username = row && row.username ? row.username : "";
        return "[" + username + "]" + nickname;
    }

    function syncUserSelectDisplayByValue() {
        if (!$("#homeUserSelect").data("combobox")) {
            return;
        }
        var userId = asNumber($("#homeUserSelect").combobox("getValue"), 0);
        if (userId <= 0) {
            return;
        }
        var rows = $("#homeUserSelect").combobox("getData");
        var matched = null;
        $.each(rows, function (idx, row) {
            if (asNumber(row.id, 0) === userId) {
                matched = row;
                return false;
            }
            return true;
        });
        if (matched) {
            $("#homeUserSelect").combobox("setText", matched.displayText || userInputText(matched));
        }
    }

    function repaintUserSelect() {
        if (!$("#homeUserSelect").data("combobox")) {
            return;
        }
        var panelWidth = $("#homePanel .user-select-input-row").width();
        if (panelWidth > 0) {
            $("#homeUserSelect").combobox("resize", panelWidth);
        }
        syncUserSelectDisplayByValue();
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
            $("#homeUserSelect").combobox("loadData", rows);
            var curUser = window.FarmAppState.currentUser || {};
            var curId = asNumber(curUser.id, 0);
            if (curId > 0) {
                $("#homeUserSelect").combobox("setValue", curId);
                $("#homeUserSelect").combobox("setText", userInputText(curUser));
                return;
            }
            if (rows.length > 0) {
                $("#homeUserSelect").combobox("setValue", rows[0].id);
                $("#homeUserSelect").combobox("setText", rows[0].displayText || userInputText(rows[0]));
            }
            repaintUserSelect();
        }, function () {
            $("#homeUserSelect").combobox("loadData", []);
        });
    }

    function initUserSelect() {
        $("#homeUserSelect").combobox({
            valueField: "id",
            textField: "displayText",
            panelCls: "user-select-combo-panel",
            panelHeight: 202,
            editable: false,
            formatter: function (row) {
                return userRowFormatter(row);
            },
            onSelect: function (row) {
                $("#homeUserSelect").combobox("setText", row.displayText || userInputText(row));
            }
        });
        loadUserOptions();
    }

    function hideBasicPanels() {
        hidePanel($("#userManagePanel"));
        hidePanel($("#homePanel"));
        hidePanel($("#plotAdminPanel"));
        hidePanel($("#seedAdminPanel"));
        hidePanel($("#settingsPanel"));
    }

    function applyShellBackground(moduleName) {
        var isHome = moduleName === "home";
        $("#appShell").toggleClass("is-homepage", isHome);
        $("#appShell").toggleClass("is-subpage", !isHome);
    }

    function setModulesInactive() {
        if (window.FarmModule) {
            window.FarmModule.setActive(false);
        }
        if (window.FarmShopModule) {
            window.FarmShopModule.setActive(false);
        }
        if (window.FarmStoreModule) {
            window.FarmStoreModule.setActive(false);
        }
        if (window.FarmPlotAdminModule) {
            window.FarmPlotAdminModule.setActive(false);
        }
        if (window.FarmUserAdminModule) {
            window.FarmUserAdminModule.setActive(false);
        }
    }

    function switchModule(moduleName) {
        window.FarmAppState.currentModule = moduleName;
        applyShellBackground(moduleName);
        $(".topbar-nav-item").removeClass("is-active");
        if (moduleName !== "home") {
            $(".topbar-nav-item[data-module='" + moduleName + "']").addClass("is-active");
        }

        hideBasicPanels();
        setModulesInactive();

        if (moduleName === "home") {
            return;
        }
        if (moduleName === "user-manage") {
            if (window.FarmUserAdminModule) {
                window.FarmUserAdminModule.setActive(true);
            } else {
                showPanel($("#userManagePanel"));
            }
            return;
        }
        if (moduleName === "user-select") {
            loadUserOptions();
            showPanel($("#homePanel"));
            window.setTimeout(function () {
                repaintUserSelect();
            }, motion().moduleEnterMs + 20);
            return;
        }
        if (moduleName === "farm") {
            if (window.FarmModule) {
                window.FarmModule.setActive(true);
            }
            return;
        }
        if (moduleName === "plot-admin") {
            if (window.FarmPlotAdminModule) {
                window.FarmPlotAdminModule.setActive(true);
            } else {
                showPanel($("#plotAdminPanel"));
            }
            return;
        }
        if (moduleName === "shop") {
            if (window.FarmShopModule) {
                window.FarmShopModule.setActive(true);
            }
            return;
        }
        if (moduleName === "store") {
            if (window.FarmStoreModule) {
                window.FarmStoreModule.setActive(true);
            }
            return;
        }
        if (moduleName === "seed-admin") {
            showPanel($("#seedAdminPanel"));
            return;
        }
        if (moduleName === "settings") {
            showPanel($("#settingsPanel"));
            if (window.FarmAudio && $.isFunction(window.FarmAudio.renderSettings)) {
                window.FarmAudio.renderSettings();
            }
            return;
        }
        $.messager.show({
            title: "提示",
            msg: "模块 " + moduleName + " 正在重构中。",
            timeout: motion().actionFeedbackMs,
            showType: "slide"
        });
    }

    function selectedUserForSwitch() {
        var userId = asNumber($("#homeUserSelect").combobox("getValue"), 0);
        if (userId <= 0) {
            return null;
        }
        var rows = $("#homeUserSelect").combobox("getData");
        var target = null;
        $.each(rows, function (idx, row) {
            if (asNumber(row.id, 0) === userId) {
                target = row;
                return false;
            }
            return true;
        });
        return target || {id: userId};
    }

    function bindEvents() {
        $(".topbar-nav-item").on("click", function () {
            if (window.FarmAudio && $.isFunction(window.FarmAudio.play)) {
                window.FarmAudio.play("click");
            }
            switchModule($(this).data("module"));
        });

        $("#homeConfirmUserBtn").on("click", function () {
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

        $(window).on("resize", function () {
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
            if (mod === "home" || mod === "user-manage" || mod === "user-select" || mod === "farm" ||
                mod === "plot-admin" || mod === "shop" || mod === "store" || mod === "seed-admin" || mod === "settings") {
                moduleName = mod;
            }
        } catch (e) {
        }
        return moduleName;
    }

    $(function () {
        initUserSelect();
        bindEvents();
        switchModule(resolveInitialModule());
        refreshCurUser();
    });

    window.FarmHomeBridge = {
        refreshCurUser: refreshCurUser,
        currentUserId: currentUserId,
        switchModule: switchModule
    };
})(window, window.jQuery);
