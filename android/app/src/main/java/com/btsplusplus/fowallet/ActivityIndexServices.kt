package com.btsplusplus.fowallet

import android.Manifest
import android.os.Bundle
import android.view.View
import bitshares.Promise
import bitshares.TempManager
import bitshares.jsonObjectfromKVS
import bitshares.xmlstring
import com.fowallet.walletcore.bts.ChainObjectManager
import com.fowallet.walletcore.bts.WalletManager
import kotlinx.android.synthetic.main.activity_index_services.*
import org.json.JSONArray

class ActivityIndexServices : BtsppActivity() {

    /**
     * 重载 - 返回键按下
     */
    override fun onBackPressed() {
        goHome()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setAutoLayoutContentView(R.layout.activity_index_services, navigationBarColor = R.color.theme01_tabBarColor)

        // 设置全屏(隐藏状态栏和虚拟导航栏)
        setFullScreen()

        // 设置底部导航栏样式
        setBottomNavigationStyle(2)

        //  设置模块可见性
        if (BuildConfig.kAppModuleEnableRank) {
            layout_diya_ranking_from_services.visibility = View.VISIBLE
        } else {
            layout_diya_ranking_from_services.visibility = View.GONE
        }
        if (BuildConfig.kAppModuleEnableFeedPrice) {
            layout_feed_price.visibility = View.VISIBLE
        } else {
            layout_feed_price.visibility = View.GONE
        }
        if (BuildConfig.kAppModuleEnableGateway) {
            layout_recharge_and_withdraw_of_service.visibility = View.VISIBLE
        } else {
            layout_recharge_and_withdraw_of_service.visibility = View.GONE
        }

        //  设置图标颜色
        val iconcolor = resources.getColor(R.color.theme01_textColorNormal)
        img_icon_transfer.setColorFilter(iconcolor)
        img_icon_qrscan.setColorFilter(iconcolor)
        img_icon_account_search.setColorFilter(iconcolor)
        img_icon_rank.setColorFilter(iconcolor)
        img_icon_voting.setColorFilter(iconcolor)
        img_icon_feedprice.setColorFilter(iconcolor)
        img_icon_deposit_withdraw.setColorFilter(iconcolor)
        img_icon_advfunction.setColorFilter(iconcolor)
        img_icon_game.setColorFilter(iconcolor)

        layout_account_query_from_services.setOnClickListener {
            TempManager.sharedTempManager().set_query_account_callback { last_activity, it ->
                last_activity.goTo(ActivityIndexServices::class.java, true, back = true)
                viewUserAssets(it.getString("name"))
            }
            goTo(ActivityAccountQueryBase::class.java, true)
        }

        if (BuildConfig.kAppModuleEnableRank) {
            layout_diya_ranking_from_services.setOnClickListener {
                goTo(ActivityMarginRanking::class.java, true)
            }
        }

        layout_transfer_from_services.setOnClickListener {
            guardWalletExist {
                val mask = ViewMask(R.string.kTipsBeRequesting.xmlstring(this), this)
                mask.show()
                val p1 = get_full_account_data_and_asset_hash(WalletManager.sharedWalletManager().getWalletAccountName()!!)
                val p2 = ChainObjectManager.sharedChainObjectManager().queryFeeAssetListDynamicInfo()
                Promise.all(p1, p2).then {
                    mask.dismiss()
                    val data_array = it as JSONArray
                    val full_userdata = data_array.getJSONObject(0)
                    goTo(ActivityTransfer::class.java, true, args = jsonObjectfromKVS("full_account_data", full_userdata))
                    return@then null
                }.catch {
                    mask.dismiss()
                    showToast(resources.getString(R.string.tip_network_error))
                }
            }
        }

        layout_voting_from_services.setOnClickListener {
            guardWalletExist { goTo(ActivityVoting::class.java, true) }
        }

        layout_saoyisao_from_services.setOnClickListener {
            this.guardPermissions(Manifest.permission.CAMERA).then {
                when (it as Int) {
                    EBtsppPermissionResult.GRANTED.value -> {
                        goTo(ActivityQrScan::class.java, true, request_code = 101)
                    }
                    EBtsppPermissionResult.SHOW_RATIONALE.value -> {
                        showToast(resources.getString(R.string.kVcScanPermissionUserRejected))
                    }
                    EBtsppPermissionResult.DONT_ASK_AGAIN.value -> {
                        showToast(resources.getString(R.string.kVcScanPermissionGotoSetting))
                    }
                }
                return@then null
            }
        }

        if (BuildConfig.kAppModuleEnableFeedPrice) {
            layout_feed_price.setOnClickListener {
                goTo(ActivityFeedPrice::class.java, true)
            }
        }

        if (BuildConfig.kAppModuleEnableGateway) {
            layout_recharge_and_withdraw_of_service.setOnClickListener {
                guardWalletExist { goTo(ActivityDepositAndWithdraw::class.java, true) }
            }
        }

        layout_advanced_feature_of_service.setOnClickListener {
            goTo(ActivityAdvancedFeature::class.java, true)
        }
    }
}
