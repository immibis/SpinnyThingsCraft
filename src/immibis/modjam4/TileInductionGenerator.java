package immibis.modjam4;


public class TileInductionGenerator extends TileMachine {
	
	@Override
	public void updateEntity() {
		
		if(worldObj.isRemote)
			initSide(getBlockMetadata());
		
		//angle += angvel;
		// angle = cable.currentPhaseAngle;

		//CableNetwork cable = getConnectedCable();
		//IShaft conn = getConnectedShaft();
		/*if(conn != null && cable != null) {
			int s_angvel = conn.getAngVel(shaftSide^1);
			int s_angle = conn.getAngle(shaftSide^1);
			
			int slip = angle - conn.getAngle(shaftSide^1);
			
			// dspeed/dt = torque / MOMENT_OF_INERTIA
			// torque is proportional to slip
			// input power (W) = input torque (kgm^2s^-2) * input speed (rad/s)
			
			// negative slip = power generated; positive slip = power consumed
			
			double torque = ShaftUtils.toDegrees(slip) * 1000;
			double genPower = -torque * ShaftUtils.toRadiansPerSecond(angvel);
			
			if(genPower > 0)
				cable.generatedPowerAcc += genPower;
			else
				cable.consumedPowerAcc -= genPower;
			
			//angle += ShaftUtils.angdiff(s_angle, angle)/16;
			
			angvel = s_angvel - ShaftUtils.fromRadiansPerSecond(torque / MOMENT_OF_INERTIA); 
			angle = cable.angle;
		}*/
	}

	void initSide(int side) {
		//shaftNode.setSideMask(side);
	}
}
