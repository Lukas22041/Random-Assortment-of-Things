package assortment_of_things.abyss.boss

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.EngagementOutcome
import com.fs.starfarer.api.combat.EngagementResultAPI
import com.fs.starfarer.api.fleet.FleetGoal
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext

class GenesisReEncounterFEContext : FleetEncounterContext() {





    override fun processEngagementResults(result: EngagementResultAPI?) {
        engagedInHostilities = true
        engagedInActualBattle = true // whether autoresolve or not, there was actual fighting


        // the fleets we get back here are combined fleets from the BattleAPI,
        // NOT the actual fleets involved, even if it's a 1 vs 1 battle.
        val winnerResult = result!!.winnerResult
        val loserResult = result!!.loserResult

        clearNoSourceMembers(winnerResult)
        clearNoSourceMembers(loserResult)


        // only happens for combat where player is involved
        val currDamageData = result!!.lastCombatDamageData
        if (currDamageData != null) {
            if (runningDamageTotal == null) {
                runningDamageTotal = currDamageData
            } else {
                runningDamageTotal.add(currDamageData)
            }
            computeFPHullDamage()
        }


        //if (winnerResult.getFleet().isPlayerFleet() || loserResult.getFleet().isPlayerFleet()) {
        /*if (battle.isPlayerInvolved) {
            FleetGoal().reportPlayerEngagement(result)
        }*/

        updateDeployedMap(winnerResult)
        updateDeployedMap(loserResult)


        //result.applyToFleets();
        //applyResultToFleets(result)


        //if (winnerResult.getFleet().isPlayerFleet() && winnerResult.getGoal() != FleetGoal.ESCAPE) {
        if (battle.isPlayerSide(winnerResult) && winnerResult.goal != FleetGoal.ESCAPE) {
            playerOnlyRetreated = false
            if (loserResult.goal == FleetGoal.ESCAPE) {
                playerPursued = true
            }
            //} else if (loserResult.getFleet().isPlayerFleet() && loserResult.getGoal() != FleetGoal.ESCAPE) {
        } else if (battle.isPlayerSide(loserResult) && loserResult.goal != FleetGoal.ESCAPE) {
            playerOnlyRetreated = false
            if (winnerResult.goal == FleetGoal.ESCAPE) {
                playerPursued = true
            }
        }

        val winnerData = getDataFor(winnerResult.fleet)
        val loserData = getDataFor(loserResult.fleet)

        winnerData.isWonLastEngagement = true
        winnerData.isEnemyCanCleanDisengage = winnerResult.enemyCanCleanDisengage()

        //winnerData.setFleetCanCleanDisengage(loserResult.enemyCanCleanDisengage());
        loserData.isWonLastEngagement = false
        loserData.isEnemyCanCleanDisengage = loserResult.enemyCanCleanDisengage()


        //loserData.setFleetCanCleanDisengage(winnerResult.enemyCanCleanDisengage());
        winnerData.isDidEnoughToDisengage = true
        var damageInFP = 0f
        for (member in winnerResult.disabled) {
            damageInFP += member.fleetPointCost.toFloat()
        }
        for (member in winnerResult.destroyed) {
            damageInFP += member.fleetPointCost.toFloat()
        }
        for (member in winnerResult.retreated) {
            damageInFP += member.fleetPointCost.toFloat()
        }


//		float remaining = 0f;
//		for (FleetMemberAPI member : winnerResult.getFleet().getFleetData().getCombatReadyMembersListCopy()) {
//			remaining += member.getFleetPointCost();
//		}
//		loserData.setDidEnoughToDisengage(damageInFP >= remaining);
        loserData.isDidEnoughToDisengage = winnerResult.enemyCanCleanDisengage()


        winnerData.lastGoal = winnerResult.goal
        loserData.lastGoal = loserResult.goal

        winnerData.deployedInLastEngagement.clear()
        winnerData.retreatedFromLastEngagement.clear()
        winnerData.inReserveDuringLastEngagement.clear()
        winnerData.disabledInLastEngagement.clear()
        winnerData.destroyedInLastEngagement.clear()
        winnerData.deployedInLastEngagement.addAll(winnerResult.deployed)
        winnerData.retreatedFromLastEngagement.addAll(winnerResult.retreated)
        winnerData.inReserveDuringLastEngagement.addAll(winnerResult.reserves)
        winnerData.disabledInLastEngagement.addAll(winnerResult.disabled)
        winnerData.destroyedInLastEngagement.addAll(winnerResult.destroyed)

        loserData.deployedInLastEngagement.clear()
        loserData.retreatedFromLastEngagement.clear()
        loserData.inReserveDuringLastEngagement.clear()
        loserData.disabledInLastEngagement.clear()
        loserData.destroyedInLastEngagement.clear()
        loserData.deployedInLastEngagement.addAll(loserResult.deployed)
        loserData.retreatedFromLastEngagement.addAll(loserResult.retreated)
        loserData.inReserveDuringLastEngagement.addAll(loserResult.reserves)
        loserData.disabledInLastEngagement.addAll(loserResult.disabled)
        loserData.destroyedInLastEngagement.addAll(loserResult.destroyed)

        for (member in loserResult.destroyed) {
            loserData.addOwn(member, FleetEncounterContextPlugin.Status.DESTROYED)
        }

        for (member in loserResult.disabled) {
            loserData.addOwn(member, FleetEncounterContextPlugin.Status.DISABLED)
        }

        for (member in winnerResult.destroyed) {
            winnerData.addOwn(member, FleetEncounterContextPlugin.Status.DESTROYED)
        }

        for (member in winnerResult.disabled) {
            winnerData.addOwn(member, FleetEncounterContextPlugin.Status.DISABLED)
        }


        //if (winnerResult.getFleet().isPlayerFleet()) {
        if (result!!.winnerResult.allEverDeployedCopy != null) {
            tallyOfficerTime(winnerData, winnerResult)
        }

        //} else if (loserResult.getFleet().isPlayerFleet()) {
        if (result!!.loserResult.allEverDeployedCopy != null) {
            tallyOfficerTime(loserData, loserResult)
        }


        // important, so that in-combat Ship objects can be garbage collected.
        // Probably some combat engine references in there, too.
        winnerResult.resetAllEverDeployed()
        getDataFor(winnerResult.fleet).memberToDeployedMap.clear()
        loserResult.resetAllEverDeployed()
        getDataFor(loserResult.fleet).memberToDeployedMap.clear()


        // moved from applyPostEngagementResult
        for (member in winnerResult.destroyed) {
            loserData.addEnemy(member, FleetEncounterContextPlugin.Status.DESTROYED)
        }
        for (member in winnerResult.disabled) {
            loserData.addEnemy(member, FleetEncounterContextPlugin.Status.DISABLED)
        }

        for (member in loserResult.destroyed) {
            winnerData.addEnemy(member, FleetEncounterContextPlugin.Status.DESTROYED)
        }
        for (member in loserResult.disabled) {
            winnerData.addEnemy(member, FleetEncounterContextPlugin.Status.DISABLED)
        }


        val winnerGoal = winnerResult.goal
        val loserGoal = loserResult.goal
        val totalWin = loserData.fleet.fleetData.membersListCopy.isEmpty()
        val playerOut = result!!.isPlayerOutBeforeEnd

        if (playerOut) {
            var playerGoal: FleetGoal? = null
            var otherGoal: FleetGoal? = null
            if (battle.isPlayerSide(battle.getSideFor(winnerResult.fleet))) {
                playerGoal = winnerGoal
                otherGoal = loserGoal
            } else {
                playerGoal = loserGoal
                otherGoal = winnerGoal
            }
            lastOutcome = if (playerGoal == FleetGoal.ATTACK) {
                if (otherGoal == FleetGoal.ATTACK) {
                    if (winnerResult.isPlayer) {
                        EngagementOutcome.BATTLE_PLAYER_OUT_FIRST_WIN
                    } else {
                        EngagementOutcome.BATTLE_PLAYER_OUT_FIRST_LOSS
                    }
                } else {
                    if (winnerResult.isPlayer) {
                        EngagementOutcome.PURSUIT_PLAYER_OUT_FIRST_WIN
                    } else {
                        EngagementOutcome.PURSUIT_PLAYER_OUT_FIRST_LOSS
                    }
                }
            } else {
                if (winnerResult.isPlayer) {
                    EngagementOutcome.ESCAPE_PLAYER_OUT_FIRST_WIN
                } else {
                    EngagementOutcome.ESCAPE_PLAYER_OUT_FIRST_LOSS
                }
            }
        } else {
            if (totalWin && winnerData.fleet.fleetData.membersListCopy.isEmpty()) {
                lastOutcome = EngagementOutcome.MUTUAL_DESTRUCTION
            } else {
                if (battle.isPlayerSide(battle.getSideFor(winnerResult.fleet))) {
                    if (winnerGoal == FleetGoal.ATTACK && loserGoal == FleetGoal.ATTACK) {
                        lastOutcome = if (totalWin) {
                            EngagementOutcome.BATTLE_PLAYER_WIN_TOTAL
                        } else {
                            EngagementOutcome.BATTLE_PLAYER_WIN
                        }
                    } else if (winnerGoal == FleetGoal.ESCAPE) {
                        lastOutcome = if (totalWin) {
                            EngagementOutcome.ESCAPE_PLAYER_WIN_TOTAL
                        } else {
                            EngagementOutcome.ESCAPE_PLAYER_WIN
                        }
                    } else if (loserGoal == FleetGoal.ESCAPE) {
                        lastOutcome = if (totalWin) {
                            EngagementOutcome.ESCAPE_ENEMY_LOSS_TOTAL
                        } else {
                            EngagementOutcome.ESCAPE_ENEMY_SUCCESS
                        }
                    }
                } else {
                    if (winnerGoal == FleetGoal.ATTACK && loserGoal == FleetGoal.ATTACK) {
                        lastOutcome = if (totalWin) {
                            EngagementOutcome.BATTLE_ENEMY_WIN_TOTAL
                        } else {
                            EngagementOutcome.BATTLE_ENEMY_WIN
                        }
                    } else if (winnerGoal == FleetGoal.ESCAPE) {
                        lastOutcome = if (totalWin) {
                            EngagementOutcome.ESCAPE_ENEMY_WIN_TOTAL
                        } else {
                            EngagementOutcome.ESCAPE_ENEMY_WIN
                        }
                    } else if (loserGoal == FleetGoal.ESCAPE) {
                        lastOutcome = if (totalWin) {
                            EngagementOutcome.ESCAPE_PLAYER_LOSS_TOTAL
                        } else {
                            EngagementOutcome.ESCAPE_PLAYER_SUCCESS
                        }
                    }
                }
            }
        }

        battle.uncombine()

        //battle.genCombinedDoNotRemoveEmpty();
        battle.genCombined()
    }
}