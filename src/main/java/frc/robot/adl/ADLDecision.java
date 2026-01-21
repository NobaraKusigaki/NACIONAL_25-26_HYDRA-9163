package frc.robot.adl;

public class ADLDecision {

    public static DecisionResult decide(
        HumanIntent intent,
        ADLState currentState,
        RobotContext context
    ) {

        if (intent == null) {
            return DecisionResult.hold(
                currentState,
                "Nenhuma intenção recebida"
            );
        }

        if (intent.getType() == HumanIntent.Type.ABORT) {
            return DecisionResult.execute(
                ADLState.EMERGENCY,
                "Abort solicitado pelo operador"
            );
        }

        if (currentState.isCritical()) {
            return DecisionResult.reject(
                currentState,
                "Robô em estado crítico: " + currentState
            );
        }

        if (intent.requiresVision()) {
            return DecisionResult.hold(
                currentState,
                "Aguardando confirmação segura da visão"
            );
        }

        if (currentState.isBusy()) {

            switch (intent.getType()) {

                case MOVE_TO_ZONE:
                    return DecisionResult.modify(
                        ADLState.MOVING,
                        "Robô ocupado, ajustando para movimento simples"
                    );

                case HOLD_POSITION:
                    return DecisionResult.execute(
                        currentState,
                        "Mantendo posição atual"
                    );

                default:
                    return DecisionResult.reject(
                        currentState,
                        "Robô ocupado, não pode executar: " + intent.getType()
                    );
            }
        }

        switch (intent.getType()) {

            case ACQUIRE_PIECE:
                return DecisionResult.execute(
                    ADLState.ACQUIRING,
                    "Iniciando aquisição de peça em "
                        + intent.getTargetZone()
                );

            case SCORE_PIECE:
                return DecisionResult.execute(
                    ADLState.SCORING,
                    "Iniciando pontuação em "
                        + intent.getTargetZone()
                );

            case MOVE_TO_ZONE:
                return DecisionResult.execute(
                    ADLState.MOVING,
                    "Movendo para zona "
                        + intent.getTargetZone()
                );

            case CLIMB:
                return DecisionResult.execute(
                    ADLState.CLIMBING,
                    "Iniciando escalada"
                );

            case HOLD_POSITION:
                return DecisionResult.execute(
                    ADLState.IDLE,
                    "Mantendo posição solicitada"
                );

            case ESCAPE:
                return DecisionResult.execute(
                    ADLState.MOVING,
                    "Executando manobra de escape"
                );

            default:
                return DecisionResult.reject(
                    currentState,
                    "Intenção desconhecida"
                );
        }
    }
}
