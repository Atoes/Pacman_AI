package pacman.entries.pacman;

import java.util.EnumMap;
import java.util.*;
import java.lang.*;
import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.controllers.examples.StarterGhosts;
import pacman.Executor;
import pacman.game.internal.Ghost;

/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getAction() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., game.entries.pacman.mypackage).
 */

class NODE{
	public NODE(Game g, MOVE m, int s){
		this.game = g; this.move = m; this.score = s;
	}
	public Game game;
	public MOVE move;
	public int score;
	public boolean visited = false;
}

public class MyPacMan extends Controller<MOVE>
{
	public int prob = 100;
	private MOVE myMove=MOVE.NEUTRAL;
	private int limit = 1;
	
	public MOVE getMove(Game game, long timeDue)
	{
		//Place your game logic here to play the game as Ms Pac-Man
		//myMove = random(game, timeDue);
		//myMove = dfs(game, timeDue);
		//myMove = iterDeep(game, timeDue);
		//myMove = bfs(game, timeDue);
		//myMove = hill_climbing(game, timeDue);
		//myMove = simulated_annealing(game, timeDue, prob);
		myMove = randomMutate(game, timeDue);
		return myMove;
	}

	public MOVE random(Game game, long time){
		MOVE[] moves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex(),game.getPacmanLastMoveMade());
		MOVE move = moves[new Random().nextInt(moves.length)];
		System.out.println(move);
		return move;
	}

	//IMPLEMENTATION OF DEPTH FIRST SEARCH
	public MOVE dfs(Game game, long time){
		int best = 0;
		MOVE bestMove = MOVE.NEUTRAL;
		int depth = 0;
		while(System.currentTimeMillis() + 3 <= time) {
			for (MOVE m : game.getPossibleMoves(game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade())) {
				Game g = game.copy();
				g.advanceGame(m, Executor.ghost.getMove(g, time));
				int result = dfsRecurse(g, time, depth, 100);
				if (result > best) {
					best = result;
					bestMove = m;
				}
			}
		}
		return bestMove;
	}
	public int dfsRecurse(Game game, long time, int depth, int limit) {
		int best = 0;
		for (Constants.GHOST ghost : Constants.GHOST.values()) {
			if (game.getGhostEdibleTime(ghost) == 0 && game.getGhostLairTime(ghost) == 0)
				if (game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(ghost)) < 15) {
					best = -9999;
					return best;
				}
				else if(game.getGhostEdibleTime(ghost)>0)
					if (game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(ghost)) < 15) {
						best = game.getScore();
						return best;
					}
		}
		if (depth == limit) {
			best = game.getScore();
			return best;
		}
		if(game.getNumberOfActivePills() + game.getNumberOfActivePowerPills() == 0){
			best = game.getScore();
			return best;
		}
		if(game.getPacmanNumberOfLivesRemaining() == 0){
			best = -999;
		}
		for(MOVE m : game.getPossibleMoves(game.getPacmanCurrentNodeIndex(),game.getPacmanLastMoveMade())) {
			Game g = game.copy();
			g.advanceGame(m, Executor.ghost.getMove(g, time));

			int result = dfsRecurse(g, time, depth + 1, limit);
			if (result > best) {
				best = result;
			}
		}
		return best;
	}

	//IMPLMENTATION OF ITERATIVE DEEPENING
	public MOVE iterDeep(Game game, long time){
		int best = 0;
		MOVE bestMove = MOVE.NEUTRAL;
		int depth = 0;
			for (MOVE m : game.getPossibleMoves(game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade())) {
				Game g = game.copy();
				g.advanceGame(m, Executor.ghost.getMove(g, time));
				int value = dfsRecurse(g, time, depth, limit);
				if (value > best) {
					best = value;
					bestMove = m;
				}
			}
			limit++;
		return bestMove;
	}

	//IMPLEMENTATION OF BREADTH FIRST SEARCH
	public MOVE bfs(Game game, long time){
		int best = 0;
		MOVE bestMove = MOVE.NEUTRAL;

		Queue<NODE> queue = new LinkedList<>();
		Queue<NODE> list = new LinkedList<>();
		for(MOVE m : game.getPossibleMoves(game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade())){
			Game c = game.copy();
			c.advanceGame(m, Executor.ghost.getMove(c,time));
			NODE node = new NODE(c,m,c.getScore());
			queue.add(node);
		}
		int index = 0;
		while(System.currentTimeMillis() +3 < time) {
			while (!queue.isEmpty()) {
				NODE n = queue.remove();
				n.visited = true;
				index = n.game.getPacmanCurrentNodeIndex();

				if(n.game.getNumberOfActivePowerPills() + n.game.getNumberOfActivePills() == 0){
					return n.move;
				}
				else if(game.wasPacManEaten()){
					n.score = -999;
				}
				else {
					for (MOVE m : n.game.getPossibleMoves(index, n.game.getPacmanLastMoveMade())) {
						if(System.currentTimeMillis() +3 < time){
							break;
						}
						Game g = n.game.copy();
						g.advanceGame(m, Executor.ghost.getMove(g,time));
						NODE node = new NODE(g, m, g.getScore());
						queue.add(node);
					}
				}
				list.add(n);
			}
			while(!list.isEmpty()){
				NODE test = list.remove();
				if(test.score > best){
					best = test.score;
					bestMove = test.move;
				}
			}
		}

		return bestMove;
	}

	//IMPLEMENTATION OF HILL CLIMBING
	public MOVE hill_climbing(Game game, long time){

		Game current = game.copy();

		int[] pills = game.getPillIndices();
		int[] powerPills = game.getPowerPillIndices();

		ArrayList<Integer> targets = new ArrayList<Integer>();
		for(int i=0;i<pills.length;i++)
			if(game.isPillStillAvailable(i))
				targets.add(pills[i]);

		for(int i=0;i<powerPills.length;i++)
			if(game.isPowerPillStillAvailable(i))
				targets.add(powerPills[i]);

		int[] targetsArray=new int[targets.size()];

		for(int i=0;i<targetsArray.length;i++)
			targetsArray[i]=targets.get(i);

		int index = game.getClosestNodeIndexFromNodeIndex(game.getPacmanCurrentNodeIndex(),targetsArray, Constants.DM.PATH);
		int best = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),index);

		while(System.currentTimeMillis() + 3 <= time) {
			for (MOVE m : current.getPossibleMoves(current.getPacmanCurrentNodeIndex())) {
				Game g = current.copy();
				g.advanceGame(m, Executor.ghost.getMove(g, time + Constants.DELAY));
				int value = g.getShortestPathDistance(g.getPacmanCurrentNodeIndex(), index);
				if(value > 5){
					return MOVE.NEUTRAL;
				}
				if (value < best) {
					return m;
				}
			}
		}
		return MOVE.NEUTRAL;
	}

	//IMPLEMENTATION OF SIMULATED ANNEALING
	public MOVE simulated_annealing(Game game, long time, int probability){

		Game current = game.copy();

		int[] pills = game.getPillIndices();
		int[] powerPills = game.getPowerPillIndices();

		ArrayList<Integer> targets = new ArrayList<Integer>();
		for(int i=0;i<pills.length;i++)
			if(game.isPillStillAvailable(i))
				targets.add(pills[i]);

		for(int i=0;i<powerPills.length;i++)
			if(game.isPowerPillStillAvailable(i))
				targets.add(powerPills[i]);

		int[] targetsArray=new int[targets.size()];

		for(int i=0;i<targetsArray.length;i++)
			targetsArray[i]=targets.get(i);

		int index = game.getClosestNodeIndexFromNodeIndex(game.getPacmanCurrentNodeIndex(),targetsArray, Constants.DM.PATH);
		int best = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),index);

		MOVE bestMove = MOVE.NEUTRAL;

		while(System.currentTimeMillis() + 3 <= time) {
			MOVE[] moves = current.getPossibleMoves(current.getPacmanCurrentNodeIndex());
			bestMove = moves[new Random().nextInt(moves.length)];

				Game g = current.copy();
				g.advanceGame(bestMove, Executor.ghost.getMove(g, time + Constants.DELAY));
				int value = g.getShortestPathDistance(g.getPacmanCurrentNodeIndex(), index);

				if(value > 3){
					int chance = new Random().nextInt(1000);
					if(chance <= prob) {
						prob /= 2;
						return bestMove;
					}
				}
				if (value < best) {
					return bestMove;
				}
		}
		return MOVE.NEUTRAL;
	}

	//EVOLUTION ALGO#1
	public MOVE randomMutate(Game game, long time){
		MOVE[] moves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());
		int size = moves.length;
		if(size % 2 == 0){
			for(int i = 0; i < moves.length/2; i++) {
				moves[i+1] = random(game, time);
			}
		}
		else{
			for(int i = 0; i < (moves.length-1)/2; i++){
				moves[i+1] = random(game,time);
			}
		}
		Game g = game.copy();
		for(int i = 0; i < moves.length; i++) {
			Game c = g.copy();
			c.advanceGame(moves[i], Executor.ghost.getMove(c, time));
			if(c.getNumberOfActivePills() + c.getNumberOfActivePowerPills() == 0){
				return c.getPacmanLastMoveMade();
			}
			else if(c.wasPacManEaten()){
				return g.getPacmanLastMoveMade();
			}
		}
		return moves[moves.length/2];
	}
}