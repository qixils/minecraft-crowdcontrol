// ReSharper disable RedundantUsingDirective
// (these imports are required by CC)
using System;
using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.Text.RegularExpressions;
using ConnectorLib;
using ConnectorLib.JSON;
using ConnectorLib.SimpleTCP;
using CrowdControl.Common;
using Newtonsoft.Json;
using ConnectorType = CrowdControl.Common.ConnectorType;
using EffectResponse = ConnectorLib.JSON.EffectResponse;
using EffectStatus = CrowdControl.Common.EffectStatus;
using Log = CrowdControl.Common.Log;
using LogLevel = CrowdControl.Common.LogLevel;
using static System.Linq.Enumerable;

namespace CrowdControl.Games.Packs;

[SuppressMessage("Interoperability", "CA1416:Validate platform compatibility")]
public class Minecraft : SimpleTCPPack<SimpleTCPClientConnector>
{
    // default port: 58731
    public override ISimpleTCPPack.PromptType PromptType => ISimpleTCPPack.PromptType.Host | ISimpleTCPPack.PromptType.Username | ISimpleTCPPack.PromptType.Password;

    public override ISimpleTCPPack.AuthenticationType AuthenticationMode => ISimpleTCPPack.AuthenticationType.SimpleTCPSendKey;

    public override ISimpleTCPPack.DigestAlgorithm AuthenticationHashMode => ISimpleTCPPack.DigestAlgorithm.SHA_512;

    public Minecraft(UserRecord player, Func<CrowdControlBlock, bool> responseHandler, Action<object> statusUpdateHandler) : base(player, responseHandler, statusUpdateHandler)
    {
        ConnectionDialogNames[ISimpleTCPPack.PromptType.Username] = "Minecraft ID";
    }

    public override Game Game => new("Minecraft", "Minecraft", "PC", ConnectorType.SimpleTCPClientConnector);
    public override EffectList Effects => new Effect[]
    {
        new("Invert Camera", "invert_look") { Price = 200, Duration = 180, Group = "clientside", Category = "Movement", Description = "Temporarily inverts mouse movement" },
        new("Invert Controls", "invert_wasd") { Price = 200, Duration = 180, Group = "clientside", Category = "Movement", Description = "Temporarily inverts WASD movement" },
        new("Disable Jumping", "disable_jumping") { Price = 100, Duration = 180, Category = "Movement", Description = "Temporarily prevents players from jumping" },
        new("Lock Camera", "camera_lock") { Price = 100, Duration = 180, Category = "Movement", Description = "Temporarily freezes every player's cameras" },
    };

    public override FunctionSet RemoteFunctions => new Dictionary<string, FunctionSet.Callback>()
    {
        {
            "known_effects", args =>
            {
                // convert object[] to list of strings
                var registeredEffects = args.Select(x => x?.ToString()?.ToLower()).ToList();
                var allEffects = Effects.Select(effect => effect.ID.ToLower());
                var unknownEffects = allEffects.Where(effect => !registeredEffects.Contains(effect));
                ReportStatus(unknownEffects, EffectStatus.MenuHidden);
                return true;
            }
        },
        {
            "__init", args =>
            {
                switch (args?.Length ?? 0)
                {
                    case 3:
                    {
                        string? host = (args![0] as string);
                        string? login = (args[1] as string);
                        string? pass = (args[2] as string);

                        if (string.IsNullOrWhiteSpace(host)) return false;
                        if (string.IsNullOrWhiteSpace(login)) return false;
                        if (string.IsNullOrWhiteSpace(pass)) return false;

                        SetConnectionInfo(host, login, pass);

                        return true;
                    }
                    case 4:
                    {
                        string? host = (args![0] as string);
                        string? login = (args[2] as string) + ':' + (args[3] as string);
                        string? pass = (args[1] as string);

                        if (string.IsNullOrWhiteSpace(host)) return false;
                        if (string.IsNullOrWhiteSpace(login)) return false;
                        if (string.IsNullOrWhiteSpace(pass)) return false;

                        SetConnectionInfo(host, login, pass);

                        return true;
                    }
                    default:
                        return false;
                }
            }
        }
    };
}