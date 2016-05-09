package algorta.domains.tetris2;


import algorta.domains.tetris.agent.AgentLinear;
import algorta.rl.DomainKnown;
import algorta.rl.Primitive;
import algorta.rl.State;
import algorta.rle.AbstractDomainKnown;
import algorta.scratch.GeneralReport;
import algorta.util.Compute;
import algorta.util.EvaluateLinearAgent;
import algorta.util.LinearDecisionRule;
import ampi.Util;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static algorta.scratch.scripts.Play.path;

public class Tetris extends AbstractDomainKnown{



    @Override
    public void getEffect(State s, Primitive a, List<State> sprimes, List<Double> tprobs) {

    }

    @Override
    public State root() {
        return new TetrisState(this);
    }

    @Override
    public List<Primitive> getPrimitives(State s) {
        return null;
    }

    @Override
    public State parseState(String stateStr) {
        return null;
    }
}
