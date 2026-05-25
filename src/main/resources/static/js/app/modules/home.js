(function (window, $) {
    function motion() {
        if ($.isFunction(window.farmMotion)) {
            return window.farmMotion();
        }
        return {moduleEnterMs: 260, actionFeedbackMs: 1200};
    }

    function showPanel($el) {
        var ms = motion().moduleEnterMs;
        $el.stop(true, true).css("display", "none").fadeIn(ms);
    }

    function hidePanel($el) {
        var ms = motion().moduleEnterMs;
        $el.stop(true, true).fadeOut(ms);
    }

    function renderTopUser(user) {
        var data = user || {};
        var head = data.head || farmResolveImg("domain/user/default-avatars/unknown-user.png");
        var nickname = data.nickname || "未知用户";
        var exp = Number(data.experience || 0);
        var coin = Number(data.coin || 0);
        var score = Number(data.score || 0);

        $("#topUserAvatar").attr("src", head);
        $("#topUserName").text(nickname);
        $("#topUserExp").text(exp);
        $("#topUserCoin").text(coin);
        $("#topUserScore").text(score);

        $("#profileAvatar").attr("src", head);
        $("#profileUserName").text(nickname);
        $("#profileUserExp").text(exp);
        $("#profileUserCoin").text(coin);
        $("#profileUserScore").text(score);
    }

    function currentUserId() {
        var user = window.FarmAppState.currentUser || {};
        var uid = Number(user.id || 0);
        return isNaN(uid) ? 0 : uid;
    }

    function syncRealtime() {
        if (!window.FarmWsBridge) {
            return;
        }
        window.FarmWsBridge.syncUser(currentUserId());
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
            syncRealtime();
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

    function initUserSelect() {
        $("#homeUserSelect").combobox({
            valueField: "id",
            textField: "nickname",
            panelHeight: 180,
            editable: false,
            method: "get",
            url: "/user/loginOptions",
            formatter: function (row) {
                var head = row.head || farmResolveImg("domain/user/default-avatars/unknown-user.png");
                return "<div style='display:flex;align-items:center;'>" +
                    "<img src='" + head + "' style='width:24px;height:24px;border-radius:4px;margin-right:6px;' alt=''>" +
                    "<span>" + (row.nickname || "") + "</span>" +
                    "</div>";
            },
            onLoadSuccess: function (rows) {
                if (rows && rows.length > 0) {
                    $("#homeUserSelect").combobox("setValue", rows[0].id);
                }
            }
        });
    }

    function hideBasicPanels() {
        hidePanel($("#profilePanel"));
        hidePanel($("#homePanel"));
        hidePanel($("#seedAdminPanel"));
        hidePanel($("#farmStage .farm-stage-watermark"));
    }

    function switchModule(moduleName) {
        var feedbackMs = motion().actionFeedbackMs;
        window.FarmAppState.currentModule = moduleName;
        $(".topbar-nav-item").removeClass("is-active");
        $(".topbar-nav-item[data-module='" + moduleName + "']").addClass("is-active");

        hideBasicPanels();

        if (window.FarmModule) {
            window.FarmModule.setActive(false);
        }
        if (window.FarmShopModule) {
            window.FarmShopModule.setActive(false);
        }
        if (window.FarmStoreModule) {
            window.FarmStoreModule.setActive(false);
        }

        if (moduleName === "profile") {
            showPanel($("#profilePanel"));
            showPanel($("#farmStage .farm-stage-watermark"));
            return;
        }

        if (moduleName === "user-select") {
            showPanel($("#homePanel"));
            showPanel($("#farmStage .farm-stage-watermark"));
            return;
        }

        if (moduleName === "farm") {
            if (window.FarmModule) {
                window.FarmModule.setActive(true);
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
            showPanel($("#farmStage .farm-stage-watermark"));
            return;
        }

        showPanel($("#farmStage .farm-stage-watermark"));
        $.messager.show({
            title: "提示",
            msg: "模块 " + moduleName + " 正在按计划重构中。",
            timeout: feedbackMs,
            showType: "slide"
        });
    }

    function bindEvents() {
        $(".topbar-nav-item").on("click", function () {
            if (window.FarmAudio && $.isFunction(window.FarmAudio.play)) {
                window.FarmAudio.play("click");
            }
            switchModule($(this).data("module"));
        });

        $("#homeConfirmUserBtn").on("click", function () {
            var userId = Number($("#homeUserSelect").combobox("getValue") || 0);
            if (!userId) {
                $.messager.alert("提示", "请先选择用户");
                return;
            }
            FarmApi.setCurUser(userId, function (res) {
                if (FarmApi.isOk(res)) {
                    refreshCurUser(function () {
                        $.messager.show({
                            title: "提示",
                            msg: "当前用户已切换",
                            timeout: motion().actionFeedbackMs,
                            showType: "slide"
                        });
                        if (window.FarmAudio && $.isFunction(window.FarmAudio.play)) {
                            window.FarmAudio.play("click");
                        }
                    });
                    return;
                }
                $.messager.alert("提示", (res && res.msg) ? res.msg : "切换失败");
            }, function () {
                $.messager.alert("提示", "切换失败，请稍后重试");
            });
        });
    }

    function resolveInitialModule() {
        var moduleName = "profile";
        try {
            var search = window.location.search || "";
            var query = new URLSearchParams(search);
            var mod = String(query.get("module") || "").toLowerCase();
            if (mod === "home") {
                moduleName = "profile";
            }
            if (mod === "profile" || mod === "user-select" || mod === "farm" || mod === "shop" || mod === "store" || mod === "seed-admin") {
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
